package com.k2archer.lib_network.retrofit.convert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class ApiGsonConverterFactory extends Converter.Factory {

    public static ApiGsonConverterFactory create() {
        return create(new Gson());
    }

    public static ApiGsonConverterFactory create(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        return new ApiGsonConverterFactory(gson);
    }

    private final Gson gson;

    private ApiGsonConverterFactory(Gson gson) {
        gson = new GsonBuilder().registerTypeAdapterFactory(new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
                final TypeAdapter<T> adapter = gson.getDelegateAdapter(this, type);
                return new TypeAdapter<T>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        adapter.write(out, value);
                    }

                    @Override
                    public T read(JsonReader in) throws IOException {
                        T value = null;
                        try {
                            value = adapter.read(in);
                        } catch (Throwable e) {
                            consumeAll(in);
//                            LogUtils.e("TAG", e.getMessage());
                        }
                        return value == null ? newObject() : value;
                    }

                    private T newObject() {
                        Class<?> t = getRawType(type.getType());
                        if (t.getName().equals(List.class.getName())) {
                            return (T) new ArrayList<>();
                        } else if (t.getName().equals(String.class.getName())) {
                            return (T) new String();
                        } else if (t.getName().equals(LinkedTreeMap.class.getName())) {
                            return (T) new LinkedTreeMap();
                        }
                        return null;
                    }

                    private void consumeAll(JsonReader in) throws IOException {
                        if (in.hasNext()) {
                            JsonToken peek = in.peek();
                            if (peek == JsonToken.STRING) {
                                in.nextString();
                            } else if (peek == JsonToken.BEGIN_ARRAY) {
                                in.beginArray();
                                consumeAll(in);
                                in.endArray();
                            } else if (peek == JsonToken.BEGIN_OBJECT) {
                                in.beginObject();
                                consumeAll(in);
                                in.endObject();
                            } else if (peek == JsonToken.END_ARRAY) {
                                in.endArray();
                            } else if (peek == JsonToken.END_OBJECT) {
                                in.endObject();
                            } else if (peek == JsonToken.NUMBER) {
                                in.nextString();
                            } else if (peek == JsonToken.BOOLEAN) {
                                in.nextBoolean();
                            } else if (peek == JsonToken.NAME) {
                                in.nextName();
                                consumeAll(in);
                            } else if (peek == JsonToken.NULL) {
                                in.nextNull();
                            }
                        }
                    }
                };
            }
        }).create();
        this.gson = gson;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        return new ResponseBodyGsonConverter<>(gson, type);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new RequestBodyGsonConverter<>(gson, adapter);
    }
}
