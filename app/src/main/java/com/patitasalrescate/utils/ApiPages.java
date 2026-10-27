package com.patitasalrescate.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Reads every page before presenting a list; prevents a partial list from looking complete. */
public final class ApiPages {
    private ApiPages() { }

    public static <P, T> void load(Function<Integer, Call<P>> fetch,
                                    Function<P, Integer> totalPages,
                                    Function<P, List<T>> items,
                                    Consumer<List<T>> success,
                                    Consumer<Throwable> failure) {
        fetchPage(1, fetch, totalPages, items, new ArrayList<>(), success, failure);
    }

    private static <P, T> void fetchPage(int page, Function<Integer, Call<P>> fetch,
                                         Function<P, Integer> totalPages,
                                         Function<P, List<T>> items, List<T> collected,
                                         Consumer<List<T>> success, Consumer<Throwable> failure) {
        fetch.apply(page).enqueue(new Callback<P>() {
            @Override public void onResponse(Call<P> call, Response<P> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    failure.accept(new IOException("API HTTP " + response.code()));
                    return;
                }
                P body = response.body();
                List<T> chunk = items.apply(body);
                if (chunk != null) collected.addAll(chunk);
                Integer lastPage = totalPages.apply(body);
                if (lastPage != null && page < lastPage) {
                    fetchPage(page + 1, fetch, totalPages, items, collected, success, failure);
                } else success.accept(collected);
            }
            @Override public void onFailure(Call<P> call, Throwable error) { failure.accept(error); }
        });
    }
}
