package com.drl.museums_geschichten.utils;

import com.drl.museums_geschichten.database.Story;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by lutz on 04/11/16.
 */
public class StoryUploader {

    //public static final String API_BASE_URL = "http://192.168.1.5:8081";
    private static final String API_BASE_URL = "http://geschichtslab.community-infrastructuring.org/wp-admin";

    private  static final String API_DATE_FORMAT = "yyyy-MM-dd";

    private  static class ServiceGenerator {

        private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        private static Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(API_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create());

        static <S> S createService(Class<S> serviceClass) {
            Retrofit retrofit = builder.client(httpClient.build()).build();
            return retrofit.create(serviceClass);
        }
    }

    interface APIService {

        @Multipart
        @POST("admin-ajax.php?action=api-call")
        Call<ResponseBody> uploadRecordingAndImage(
            @Part("title") RequestBody title,
            @Part("text") RequestBody text,
            @Part("zeit") RequestBody date,
            @Part("lat") RequestBody latitude,
            @Part("lng") RequestBody longitude,
            @Part MultipartBody.Part recording,
            @Part MultipartBody.Part image);
    }

    public static Call<ResponseBody> upload(Story story, Callback<ResponseBody> callback) {
        // create upload service client
        APIService service = ServiceGenerator.createService(APIService.class);

        // audio record
        MultipartBody.Part audioFile = MultipartBody.Part.createFormData("_","_");
        if (story.audioFile != null) {
            File file = new File(story.audioFile);
            if (file.exists())
                audioFile = prepareFilePart("audio_file", file);
        }

        // picture
        MultipartBody.Part imageFile = MultipartBody.Part.createFormData("_","_");
        if (story.imageFile != null) {
            File file = new File(story.imageFile);
            if (file.exists())
                imageFile = prepareFilePart("audio_bild", file);
        }

        // add other data
        RequestBody title = createPartFromString(story.title == null ? "" : story.title);
        RequestBody text = createPartFromString(story.text == null ? "" : story.text);
        RequestBody date = createPartFromString(story.date == null ? "" : formatDate(story.getDate()));
        RequestBody latitude = createPartFromString(story.loc_name == null ? "" : Double.toString(story.loc_latitude));
        RequestBody longitude = createPartFromString(story.loc_name == null ? "" : Double.toString(story.loc_longitude));

        // execute the request
        Call<ResponseBody> call = service.uploadRecordingAndImage(title, text, date, latitude, longitude, audioFile, imageFile );
        call.enqueue(callback);

        return call;
    }

    private  static String formatDate(Date date) {

        if (date == null)
            return "";

        SimpleDateFormat outputFormat = new SimpleDateFormat(API_DATE_FORMAT);
        return outputFormat.format(date);
    }

    private  static final String MULTIPART_FORM_DATA = "multipart/form-data";

    private  static RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), descriptionString);
    }

    private  static MultipartBody.Part prepareFilePart(String partName, File file) {

        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

}
