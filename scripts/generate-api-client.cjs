// Regenerate the typed API boundary from the checked-in, reviewed OpenAPI snapshot.
const fs = require('fs');
const path = require('path');
const root = path.resolve(__dirname, '..');
const spec = JSON.parse(fs.readFileSync(path.join(root, 'docs/api/openapi.json'), 'utf8'));
const base = path.join(root, 'app/src/main/java/com/patitasalrescate/data');
const write = (file, text) => { fs.mkdirSync(path.dirname(file), { recursive: true }); fs.writeFileSync(file, text); };
const ref = s => s.$ref?.split('/').pop();
function type(s) {
  if (ref(s)) return ref(s);
  if (s.format === 'binary') return 'UploadFile';
  if (s.type === 'array') return `java.util.List<${type(s.items)}>`;
  if (s.type === 'object') return `java.util.Map<String, ${type(s.additionalProperties)}>`;
  return { string: 'String', integer: 'Integer', number: 'Double', boolean: 'Boolean' }[s.type];
}
for (const [name, schema] of Object.entries(spec.components.schemas)) {
  let code = `package com.patitasalrescate.data.remote.dto;\n\n`;
  if (schema.enum) code += `public enum ${name} { ${schema.enum.join(', ')} }\n`;
  else {
    code += `import com.google.gson.annotations.SerializedName;\n\n/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */\npublic final class ${name} {\n`;
    for (const [field, s] of Object.entries(schema.properties)) code += `    @SerializedName("${field}")\n    public ${type(s)} ${field};\n`;
    code += '}\n';
  }
  write(path.join(base, 'remote/dto', name + '.java'), code);
}
const names = { 'Borrar Usuario': 'deleteUser', 'Iniciar Sesión': 'login', 'Registrarse': 'register', 'Verificar Correo': 'verifyEmail', 'Enviar Correo de Verificación': 'sendVerificationEmail' };
const groups = { 'Administración': 'Admin', 'Autenticación': 'Auth', 'Mascotas': 'Pet', 'Refugios': 'Shelter', 'PatitasAPI': 'Status', 'Eventos': 'Event' };
const operations = [];
for (const [url, verbs] of Object.entries(spec.paths)) for (const [verb, op] of Object.entries(verbs)) {
  const group = groups[op.tags[0]];
  const name = names[op.operationId] || (op.operationId ? op.operationId[0].toLowerCase() + op.operationId.slice(1) : 'getStatus');
  const response = Object.values(op.responses)[0];
  const responseSchema = response.content && Object.values(response.content)[0].schema;
  const result = responseSchema ? type(responseSchema) : 'Void';
  const content = op.requestBody?.content;
  const multipart = !!content?.['multipart/form-data'];
  const bodySchema = content && Object.values(content)[0].schema;
  const bodyType = bodySchema && (ref(bodySchema) || 'UploadFile');
  const params = (op.parameters || []).map(p => ({ name: p.name, type: type(p.schema), annotation: `@${p.in === 'path' ? 'Path' : 'Query'}("${p.name}")`, required: p.required }));
  const args = params.map(p => `${p.type} ${p.name}`);
  if (bodyType) args.push(`${bodyType} body`);
  const signature = `Call<${result}> ${name}(${args.join(', ')})`;
  let serviceArgs = params.map(p => `${p.annotation} ${p.type} ${p.name}`);
  if (multipart) serviceArgs.push('@Part java.util.List<okhttp3.MultipartBody.Part> parts');
  else if (bodyType) serviceArgs.push(`@Body ${bodyType} body`);
  const annotation = `    ${multipart ? '@Multipart\n    ' : ''}@${verb.toUpperCase()}("${url.slice(1) || './'}")`;
  let implementation = '';
  for (const p of params.filter(p => p.required)) implementation += `        java.util.Objects.requireNonNull(${p.name}, "${p.name}");\n`;
  for (const p of params.filter(p => p.name === 'page' || p.name === 'pageSize')) implementation += `        if (${p.name} != null && ${p.name} < 1) throw new IllegalArgumentException("${p.name} must be positive");\n`;
  if (bodyType) implementation += '        java.util.Objects.requireNonNull(body, "body");\n';
  if (multipart) {
    implementation += '        FormParts parts = new FormParts();\n';
    if (ref(bodySchema)) for (const [f, s] of Object.entries(spec.components.schemas[bodyType].properties)) {
      implementation += `        parts.${s.type === 'array' ? 'files' : s.format === 'binary' ? 'file' : 'field'}("${f}", body.${f});\n`;
    } else implementation += '        parts.file("photo", body);\n';
  }
  const callArgs = params.map(p => p.name).concat(bodyType ? [multipart ? 'parts.build()' : 'body'] : []);
  implementation += `        return service.${name}(${callArgs.join(', ')});\n`;
  operations.push({ group, name, url, verb, signature, annotation, serviceArgs, result, implementation, args, callArgs: params.map(p => p.name).concat(bodyType ? ['body'] : []), policy: op['x-roles-policy'] || 'Public', cooldown: op['x-cooldown'] || '' });
}
for (const group of Object.values(groups)) {
  const ops = operations.filter(o => o.group === group);
  const imports = 'import com.patitasalrescate.data.remote.dto.*;\nimport retrofit2.Call;\n';
  write(path.join(base, 'source', `I${group}ApiDataSource.java`), `package com.patitasalrescate.data.source;\n\n${imports}\n/** Remote operations; enqueue calls off the UI thread. */\npublic interface I${group}ApiDataSource {\n${ops.map(o => `    ${o.signature};`).join('\n')}\n}\n`);
  write(path.join(base, 'remote', `${group}ApiService.java`), `package com.patitasalrescate.data.remote;\n\n${imports}import retrofit2.http.*;\n\ninterface ${group}ApiService {\n${ops.map(o => `${o.annotation}\n    Call<${o.result}> ${o.name}(${o.serviceArgs.join(', ')});`).join('\n\n')}\n}\n`);
  write(path.join(base, 'remote', `${group}ApiDataSource.java`), `package com.patitasalrescate.data.remote;\n\n${imports}import com.patitasalrescate.data.source.I${group}ApiDataSource;\n\npublic final class ${group}ApiDataSource implements I${group}ApiDataSource {\n    private final ${group}ApiService service;\n\n    public ${group}ApiDataSource(retrofit2.Retrofit retrofit) {\n        service = retrofit.create(${group}ApiService.class);\n    }\n\n${ops.map(o => `    @Override public ${o.signature} {\n${o.implementation}    }`).join('\n\n')}\n}\n`);
  write(path.join(base, 'repository', `${group}ApiRepository.java`), `package com.patitasalrescate.data.repository;\n\n${imports}import com.patitasalrescate.data.source.I${group}ApiDataSource;\n\n/** API repository, injectable independently of the existing local repositories. */\npublic final class ${group}ApiRepository {\n    private final I${group}ApiDataSource source;\n\n    public ${group}ApiRepository(I${group}ApiDataSource source) {\n        this.source = java.util.Objects.requireNonNull(source);\n    }\n\n${ops.map(o => `    public ${o.signature} {\n        return source.${o.name}(${o.callArgs.join(', ')});\n    }`).join('\n\n')}\n}\n`);
}
write(path.join(root, 'docs/api/endpoints.md'), '# Operaciones implementadas\n\nContrato: https://api-patitasalrescate.galaxym4.dev/swagger/v1/swagger.json\n\n| Repositorio | Método | HTTP | Ruta | Rol | Cooldown |\n|---|---|---|---|---|---|\n' + operations.map(o => `| ${o.group}ApiRepository | ${o.name} | ${o.verb.toUpperCase()} | ${o.url} | ${o.policy} | ${o.cooldown} |`).join('\n') + '\n');
console.log(`Generated ${operations.length} operations and ${Object.keys(spec.components.schemas).length} schemas.`);
