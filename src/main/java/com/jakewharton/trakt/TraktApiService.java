package com.jakewharton.trakt;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.type.TypeReference;
import com.jakewharton.apibuilder.ApiService;
import com.jakewharton.trakt.entities.TvShowEpisode;
import com.jakewharton.trakt.entities.TvShowSeason;
import com.jakewharton.trakt.entities.WatchedMediaEntity;
import com.jakewharton.trakt.enumerations.ActivityAction;
import com.jakewharton.trakt.enumerations.ActivityType;
import com.jakewharton.trakt.enumerations.DayOfTheWeek;
import com.jakewharton.trakt.enumerations.Gender;
import com.jakewharton.trakt.enumerations.ListItemType;
import com.jakewharton.trakt.enumerations.ListPrivacy;
import com.jakewharton.trakt.enumerations.MediaType;
import com.jakewharton.trakt.enumerations.Rating;
import com.jakewharton.trakt.enumerations.RatingType;
import com.jakewharton.trakt.util.Base64;

/**
 * Trakt-specific API service extension which facilitates provides helper
 * methods for performing remote method calls as well as deserializing the
 * corresponding JSON responses.
 *
 * @author Jake Wharton <jakewharton@gmail.com>
 */
public abstract class TraktApiService extends ApiService {
    /** Default connection timeout (in milliseconds). */
    private static final int DEFAULT_TIMEOUT_CONNECT = 60 * (int)TraktApiBuilder.MILLISECONDS_IN_SECOND;

    /** Default read timeout (in milliseconds). */
    private static final int DEFAULT_TIMEOUT_READ = 60 * (int)TraktApiBuilder.MILLISECONDS_IN_SECOND;

    /** HTTP header name for authorization. */
    private static final String HEADER_AUTHORIZATION = "Authorization";

    /** HTTP authorization type. */
    private static final String HEADER_AUTHORIZATION_TYPE = "Basic";

    /** Character set used for encoding and decoding transmitted values. */
    //XXX private static final Charset UTF_8_CHAR_SET = Charset.forName(ApiService.CONTENT_ENCODING);

    /** HTTP post method name. */
    private static final String HTTP_METHOD_POST = "POST";

    /** Format for decoding JSON dates in string format. */
    private static final SimpleDateFormat JSON_STRING_DATE = new SimpleDateFormat("yyy-MM-dd");

    /** Default plugin version debug string. */
    private static final String DEFAULT_PLUGIN_VERSION = Version.FULL;

    /** Default media center version debug string. */
    private static final String DEFAULT_MEDIA_CENTER_VERSION = Version.FULL;

    /** Default media center build date debug string. */
    private static final String DEFAULT_MEDIA_CENTER_DATE = Version.DATE;

    /** Default application name debug string. */
    private static final String DEFAULT_APP_DATE = Version.DATE;

    /** Default application version debug string. */
    private static final String DEFAULT_APP_VERSION = Version.FULL;

    /** Time zone for Trakt dates. */
    private static final TimeZone TRAKT_TIME_ZONE = TimeZone.getTimeZone("GMT-8:00");


    /** API key. */
    private String apiKey;

    /** Plugin version debug string. */
    private String pluginVersion;

    /** Media center version debug string. */
    private String mediaCenterVersion;

    /** Media center build date debug string. */
    private String mediaCenterDate;

    /** Application date debug string. */
    private String appDate;

    /** Application version debug string. */
    private String appVersion;

    /** Whether or not to use SSL API endpoint. */
    private boolean useSsl;


    /**
     * Create a new Trakt service with our proper default values.
     */
    public TraktApiService() {
        //Setup timeout defaults
        this.setConnectTimeout(DEFAULT_TIMEOUT_CONNECT);
        this.setReadTimeout(DEFAULT_TIMEOUT_READ);

        //Setup debug string defaults
        this.setPluginVersion(DEFAULT_PLUGIN_VERSION);
        this.setMediaCenterVersion(DEFAULT_MEDIA_CENTER_VERSION);
        this.setMediaCenterDate(DEFAULT_MEDIA_CENTER_DATE);
        this.setAppDate(DEFAULT_APP_DATE);
        this.setAppVersion(DEFAULT_APP_VERSION);
    }


    /**
     * Execute request using HTTP GET.
     *
     * @param url URL to request.
     * @return JSON object.
     */
    public InputStream get(String url) {
        return this.executeGet(url);
    }

    /**
     * Execute request using HTTP POST.
     *
     * @param url URL to request.
     * @param postBody String to use as the POST body.
     * @return JSON object.
     */
    public InputStream post(String url, String postBody) {
        return this.executeMethod(url, postBody, null, HTTP_METHOD_POST, HttpURLConnection.HTTP_OK);
    }

    /**
     * Set email and password to use for HTTP basic authentication.
     *
     * @param username Username.
     * @param password_sha Password SHA1.
     */
    public void setAuthentication(String username, String password_sha) {
        if ((username == null) || (username.length() == 0)) {
            throw new IllegalArgumentException("Username must not be empty.");
        }
        if ((password_sha == null) || (password_sha.length() == 0)) {
            throw new IllegalArgumentException("Password SHA must not be empty.");
        }

        String source = username + ":" + password_sha;
        String authentication = HEADER_AUTHORIZATION_TYPE + " " + Base64.encodeBytes(source.getBytes());

        this.addRequestHeader(HEADER_AUTHORIZATION, authentication);
    }

    /**
     * Get the API key.
     *
     * @return Value
     */
    /*package*/ String getApiKey() {
        return this.apiKey;
    }

    /**
     * Set API key to use for client authentication by Trakt.
     *
     * @param value Value.
     */
    public void setApiKey(String value) {
        this.apiKey = value;
    }

    /**
     * Get the plugin version debug string used for scrobbling.
     *
     * @return Value.
     */
    /*package*/ String getPluginVersion() {
        return pluginVersion;
    }

    /**
     * Set the plugin version debug string used for scrobbling.
     *
     * @param pluginVersion Value.
     */
    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    /**
     * Get the media center version debug string used for scrobbling.
     *
     * @return Value.
     */
    /*package*/ String getMediaCenterVersion() {
        return mediaCenterVersion;
    }

    /**
     * Set the media center version debug string used for scrobbling.
     *
     * @param mediaCenterVersion Value.
     */
    public void setMediaCenterVersion(String mediaCenterVersion) {
        this.mediaCenterVersion = mediaCenterVersion;
    }

    /**
     * Get the media center build date debug string used for scrobbling.
     *
     * @return Value.
     */
    /*package*/ String getMediaCenterDate() {
        return mediaCenterDate;
    }

    /**
     * Set the media center build date debug string used for scrobbling.
     *
     * @param mediaCenterDate Value.
     */
    public void setMediaCenterDate(String mediaCenterDate) {
        this.mediaCenterDate = mediaCenterDate;
    }

    /**
     * Get the application date debug string used for checking in.
     *
     * @return Value.
     */
    /*package*/ String getAppDate() {
        return appDate;
    }

    /**
     * Set the application date debug string used for checking in.
     *
     * @param appDate Value.
     */
    public void setAppDate(String appDate) {
        this.appDate = appDate;
    }

    /**
     * Get the application version debug string used for checking in.
     *
     * @return Value.
     */
    /*package*/ String getAppVersion() {
        return appVersion;
    }

    /**
     * Set the application version debug string used for checking in.
     *
     * @param appVersion Value.
     */
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    /**
     * Get whether or not we want to use the SSL API endpoint.
     *
     * @return Value.
     */
    /*package*/ boolean getUseSsl() {
        return useSsl;
    }

    /**
     * Set whether or not to use the SSL API endpoint.
     *
     * @param useSsl Value.
     */
    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }

    /**
     * Use Jackson to deserialize a JSON object to a native class representation.
     *
     * @param <T> Native class type.
     * @param typeToken Native class type wrapper.
     * @param response Serialized JSON object.
     * @return Deserialized native instance.
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    @SuppressWarnings("unchecked")
    protected <T> T unmarshall(TypeReference<T> typeToken, InputStream response) throws JsonParseException, JsonMappingException, IOException {
        return (T)TraktApiService.getObjectMapper().readValue(response, typeToken);
    }

    /**
     * Use Jackson to deserialize a JSON string to a native class representation.
     *
     * @param <T> Native class type.
     * @param typeToken Native class type wrapper.
     * @param reponse Serialized JSON string.
     * @return Deserialized native instance.
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    @SuppressWarnings("unchecked")
    protected <T> T unmarshall(TypeReference<T> typeToken, String reponse) throws JsonParseException, JsonMappingException, IOException {
        return (T)TraktApiService.getObjectMapper().readValue(reponse, typeToken);
    }





    private static ObjectMapper MAPPER = null;
    
    static ObjectMapper getObjectMapper() {
        if (MAPPER == null) {
            MAPPER = createObjectMapper();
        }
        return MAPPER;
    }

    /**
     * Create an {@link ObjectMapper} and register all of the custom types needed
     * in order to properly deserialize complex Trakt-specific type.
     *
     * @return Assembled Jackson builder instance.
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule traktModule = new SimpleModule("trakt", new org.codehaus.jackson.Version(1, 0, 0, null));

        //class types
        traktModule.addDeserializer(Integer.class, new JsonDeserializer<Integer> () {
            @Override
            public Integer deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
                try {
                    return new Integer(arg0.getIntValue());
                } catch (JsonParseException e) {
                    return null;
                }
            }
        });
        traktModule.addDeserializer(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
                try {
                    long value = arg0.getLongValue();
                    Calendar date = Calendar.getInstance(TRAKT_TIME_ZONE);
                    date.setTimeInMillis(value * TraktApiBuilder.MILLISECONDS_IN_SECOND);
                    return date.getTime();
                } catch (JsonParseException outer) {
                    try {
                        return JSON_STRING_DATE.parse(arg0.getText());
                    } catch (ParseException inner) {
                        throw outer;
                    }
                }
            }
        });
        traktModule.addDeserializer(Calendar.class, new JsonDeserializer<Calendar>() {
            @Override
            public Calendar deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
                Calendar value = Calendar.getInstance(TRAKT_TIME_ZONE);
                value.setTimeInMillis(arg0.getLongValue() * TraktApiBuilder.MILLISECONDS_IN_SECOND);
                return value;
            }
        });
        traktModule.addDeserializer(TvShowSeason.Episodes.class, new JsonDeserializer<TvShowSeason.Episodes>() {
            @Override
            public TvShowSeason.Episodes deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
                TvShowSeason.Episodes episodes = new TvShowSeason.Episodes();
                try {
                    if (arg0.isExpectedStartArrayToken()) {
                        JsonToken token = arg0.nextToken();
                        Boolean isInteger = null;
                        List<Integer> asInteger = null;
                        List<TvShowEpisode> asEpisode = null;
                        while (token != JsonToken.END_ARRAY) {
                            if (isInteger == null) {
                                isInteger = token.isNumeric();
                                if (isInteger) {
                                    asInteger = new ArrayList<Integer>();
                                } else {
                                    asEpisode = new ArrayList<TvShowEpisode>();
                                }
                            }
                            if (isInteger) {
                                asInteger.add(arg0.getValueAsInt());
                            } else {
                                
                            }
                        }
                        if (arg0.getAsJsonArray().get(0).isJsonPrimitive()) {
                            //Episode number list
                            Field fieldNumbers = TvShowSeason.Episodes.class.getDeclaredField("numbers");
                            fieldNumbers.setAccessible(true);
                            fieldNumbers.set(episodes, arg0.readValueAs(new TypeReference<List<Integer>>() {}));
                        } else {
                            //Episode object list
                            Field fieldList = TvShowSeason.Episodes.class.getDeclaredField("episodes");
                            fieldList.setAccessible(true);
                            fieldList.set(episodes, arg0.readValueAs(new TypeReference<List<TvShowEpisode>>() {}));
                        }
                    } else {
                        //Episode count
                        Field fieldCount = TvShowSeason.Episodes.class.getDeclaredField("count");
                        fieldCount.setAccessible(true);
                        fieldCount.set(episodes, new Integer(arg0.getIntValue()));
                    }
                } catch (SecurityException e) {
                    throw new JsonParseException(e);
                } catch (NoSuchFieldException e) {
                    throw new JsonParseException(e);
                } catch (IllegalArgumentException e) {
                    throw new JsonParseException(e);
                } catch (IllegalAccessException e) {
                    throw new JsonParseException(e);
                }
                return episodes;
            }
        });
        traktModule.addDeserializer(WatchedMediaEntity.class, new JsonDeserializer<WatchedMediaEntity>() {
            @Override
            public WatchedMediaEntity deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
                if (arg0.isExpectedStartArrayToken()) {
                    return null;
                } else {
                    return arg0.readValueAs(WatchedMediaEntity.class);
                }
            }
        });

        //enum types
        traktModule.addDeserializer(ActivityAction.class, new JsonDeserializer<ActivityAction>() {
            @Override
            public ActivityAction deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
                return ActivityAction.fromValue(arg0.getText());
            }
        });
        traktModule.addDeserializer(ActivityType.class, new JsonDeserializer<ActivityType>() {
            @Override
            public ActivityType deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
                return ActivityType.fromValue(arg0.getText());
            }
        });
        traktModule.addDeserializer(DayOfTheWeek.class, new JsonDeserializer<DayOfTheWeek>() {
            @Override
            public DayOfTheWeek deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
                return DayOfTheWeek.fromValue(arg0.getText());
            }
        });
        traktModule.addDeserializer(Gender.class, new JsonDeserializer<Gender>() {
            @Override
            public Gender deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
                return Gender.fromValue(arg0.getText());
            }
        });
        traktModule.addDeserializer(ListItemType.class, new JsonDeserializer<ListItemType>() {
            @Override
            public ListItemType deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
                return ListItemType.fromValue(arg0.getText());
            }
        });
        traktModule.addDeserializer(ListPrivacy.class, new JsonDeserializer<ListPrivacy>() {
            @Override
            public ListPrivacy deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
                return ListPrivacy.fromValue(arg0.getText());
            }
        });
        traktModule.addDeserializer(MediaType.class, new JsonDeserializer<MediaType>() {
            @Override
            public MediaType deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
                return MediaType.fromValue(arg0.getText());
            }
        });
        traktModule.addDeserializer(Rating.class, new JsonDeserializer<Rating>() {
            @Override
            public Rating deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
                return Rating.fromValue(arg0.getText());
            }
        });
        traktModule.addSerializer(Rating.class, new JsonSerializer<Rating>() {
            @Override
            public void serialize(Rating arg0, JsonGenerator arg1, SerializerProvider arg2) throws IOException, JsonProcessingException {
                arg1.writeString(arg0.toString()); //XXX Check if this happens by default
            }
        });
        traktModule.addDeserializer(RatingType.class, new JsonDeserializer<RatingType>() {
            @Override
            public RatingType deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
                return RatingType.fromValue(arg0.getText());
            }
        });

        mapper.registerModule(traktModule);
        return mapper;
    }
}
