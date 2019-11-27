package id.ac.unpad.laporapp.http;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiInterface {

    @Multipart
    @POST("lapor/save")
    Call<String> upload(@Part MultipartBody.Part part,
                        @Part("lokasi") RequestBody lokasi,
                        @Part("keterangan") RequestBody keterangan,
                        @Part("waktu") RequestBody waktu,
                        @Part("pil") RequestBody pil,
                        @Part("type") RequestBody type,
                        @Part("latitude") RequestBody latitude,
                        @Part("longitude") RequestBody longitude
    );

}
