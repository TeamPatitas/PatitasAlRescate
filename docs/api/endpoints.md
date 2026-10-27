# Operaciones implementadas

Contrato: https://api-patitasalrescate.galaxym4.dev/swagger/v1/swagger.json

| Repositorio | Método | HTTP | Ruta | Rol | Cooldown |
|---|---|---|---|---|---|
| AdminApiRepository | healthCheck | GET | /admin/health | DevOnly | 30 segundos |
| AdminApiRepository | getCurrentUser | GET | /admin/user | User |  |
| AdminApiRepository | updateCurrentUser | PATCH | /admin/user | User |  |
| AdminApiRepository | deleteUser | DELETE | /admin/user/{id} | User |  |
| AdminApiRepository | addRoles | PATCH | /admin/add-roles | DevOnly |  |
| AdminApiRepository | removeRoles | PATCH | /admin/remove-roles | DevOnly |  |
| AdminApiRepository | getAllUsers | GET | /admin/users | DevOnly |  |
| AdminApiRepository | enableShelter | PATCH | /admin/shelter/enable/{id} | DevOnly | 5 minutos |
| AdminApiRepository | disableShelter | PATCH | /admin/shelter/disable/{id} | DevOnly | 5 minutos |
| AuthApiRepository | login | POST | /auth/login | Public |  |
| AuthApiRepository | register | POST | /auth/register | Public |  |
| AuthApiRepository | verifyEmail | GET | /auth/verify-email | Public |  |
| AuthApiRepository | sendVerificationEmail | GET | /auth/send-verification-email | User | 1 minuto |
| EventApiRepository | createEvent | POST | /event | ShelterOwner |  |
| EventApiRepository | getAllEvents | GET | /event | User |  |
| EventApiRepository | getEventById | GET | /event/{eventId} | User |  |
| EventApiRepository | updateEvent | PATCH | /event/{eventId} | ShelterOwner |  |
| EventApiRepository | deleteEvent | DELETE | /event/{eventId} | ShelterOwner |  |
| PetApiRepository | createPet | POST | /pet | ShelterOwner |  |
| PetApiRepository | getAllPets | GET | /pet | User |  |
| PetApiRepository | getPetById | GET | /pet/{petId} | User |  |
| PetApiRepository | updatePet | PATCH | /pet/{petId} | ShelterOwner |  |
| PetApiRepository | deletePet | DELETE | /pet/{petId} | ShelterOwner |  |
| PetApiRepository | updatePetPhoto | PATCH | /pet/{petId}/photo/{photoIndex} | ShelterOwner |  |
| StatusApiRepository | getStatus | GET | / | Public |  |
| ShelterApiRepository | createShelter | POST | /shelter | User |  |
| ShelterApiRepository | getAllShelters | GET | /shelter | User |  |
| ShelterApiRepository | updateShelter | PATCH | /shelter/{id} | User |  |
| ShelterApiRepository | getShelterById | GET | /shelter/{id} | User |  |
| ShelterApiRepository | deleteShelter | DELETE | /shelter/{id} | ShelterOwner |  |
