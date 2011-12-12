package com.jakewharton.trakt.services;

import java.util.List;
import org.codehaus.jackson.type.TypeReference;
import com.jakewharton.trakt.TraktApiBuilder;
import com.jakewharton.trakt.TraktApiService;
import com.jakewharton.trakt.entities.Genre;

public class GenreService extends TraktApiService {
    public MoviesBuilder movies() {
        return new MoviesBuilder(this);
    }

    public ShowsBuilder shows() {
        return new ShowsBuilder(this);
    }

    public static final class MoviesBuilder extends TraktApiBuilder<List<Genre>> {
        private static final String URI = "/genres/movies.json/" + FIELD_API_KEY;

        private MoviesBuilder(GenreService service) {
            super(service, new TypeReference<List<Genre>>() {}, URI);
        }
    }
    public static final class ShowsBuilder extends TraktApiBuilder<List<Genre>> {
        private static final String URI = "/genres/shows.json/" + FIELD_API_KEY;

        private ShowsBuilder(GenreService service) {
            super(service, new TypeReference<List<Genre>>() {}, URI);
        }
    }
}
