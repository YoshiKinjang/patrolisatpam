package com.example.patrolisatpam.retrofit




import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*


//sebenarnya bukan cuman untuk login tp nama filenya terlanjur login :V
interface APIService {
    @POST("login")
    @FormUrlEncoded
    fun registrationPost(@Field("username") username: String,
                         @Field("password") password: String): Call<ResponseBody>
}
interface getPos {
    @GET("get_pos")
    fun registrationGet(@Query("noind") noind: String?,
                        @Query("ronde") ronde: Int?): Call<ResponseBody>
}
interface insScan{
    @POST("ins_ptrl_scan")
    @FormUrlEncoded
    fun dataPost(
        @Field("noind") noind: String?,
        @Field("lat") lat: String,
        @Field("long") long: String,
        @Field("ronde") ronde: String?,
        @Field("pos") pos: String?,
        @Field("tanggal") tanggal: String?,
        @Field("kode") kode: String): Call<ResponseBody>
}

interface insTemuan{
    @POST("ins_temuan")
    @FormUrlEncoded
    fun dataPostTemuan(
        @Field("noind") noind: String?,
        @Field("lat") lat: String?,
        @Field("long") long: String?,
        @Field("deskripsi") deskripsi: String?,
        @Field("id_patroli") patroli: Int): Call<ResponseBody>
}

interface cekPos {
    @GET("cek_pos")
    fun cekAktivityPos(
        @Query("noind") noind: String?,
        @Query("ronde") ronde: String?,
        @Query("pos") pos: String?
    ): Call<ResponseBody>
}

interface cekTemuan {
    @GET("cek_temuan")
    fun cekAktivityTemuan(
        @Query("id") id: Int?
    ): Call<ResponseBody>
}

interface cekPertanyaan {
    @GET("cek_pos_pertanyaan")
    fun cekAktivityPertanyaan(
        @Query("id") id: Int?
    ): Call<ResponseBody>
}

interface getPertanyaan {
    @GET("get_pertanyaan")
    fun getListPertanyaan(
        @Query("id") id: Int?,
        @Query("id_patroli") id_patroli: Int?
    ): Call<ResponseBody>
}

interface saveJawaban{
    @POST("ins_jawaban")
    @FormUrlEncoded
    fun dataPostJawaban(
        @Field("arr") arr: JSONObject?
    ): Call<ResponseBody>
}

interface upImg{
    @POST("upimg")
//    @Headers( "Content-Type: application/json;charset=UTF-8")
    fun dataPostImg(
        @Body file: RequestBody?
    ): Call<ResponseBody>
}

interface getImg{
    @GET("get_atach_id")
    fun value(
        @Query("id_patroli") id: Int?
    ): Call<ResponseBody>
}

interface getProfile{
    @GET("get_profile")
    fun valuegp(
        @Query("noind") noind: String?
    ): Call<ResponseBody>
}

interface getRonde{
    @GET("list_ronde")
    fun ronde(): Call<ResponseBody>
}

interface getRondeID{
    @GET("list_rondeID")
    fun ronde(@Query("id") noind: Int?): Call<ResponseBody>
}

interface getQr{
    @GET("get_qr_patroli")
    fun idPatroli(
        @Query("id_patroli") id: Int?
    ): Call<ResponseBody>
}

object ApiLogin {
    val BASE_URL = "http://erp.quick.com/SiteManagement/MobileOrder/"
    val apiService: APIService
        get() = RetrofitClient.getClient(BASE_URL)!!.create(APIService::class.java)
}

object ApiUtils {

    val BASE = "http://192.168.168.128/khs-erp-patroli/PatroliSatpam/api/"
//    val BASE = "http://erp.quick.com/PatroliSatpam/api/"

    val getListPos: getPos
        get() = RetrofitClient.getClient(BASE)!!.create(getPos::class.java)

    val insertScan: insScan
        get() = RetrofitClient.getClient(BASE)!!.create(insScan::class.java)

    val checkPos: cekPos
        get() = RetrofitClient.getClient(BASE)!!.create(cekPos::class.java)

    val insertTemuan: insTemuan
        get() = RetrofitClient.getClient(BASE)!!.create(insTemuan::class.java)

    val checkTemuan: cekTemuan
        get() = RetrofitClient.getClient(BASE)!!.create(cekTemuan::class.java)

    val checkPertanyaan: cekPertanyaan
        get() = RetrofitClient.getClient(BASE)!!.create(cekPertanyaan::class.java)

    val listPertanyaan: getPertanyaan
        get() = RetrofitClient.getClient(BASE)!!.create(getPertanyaan::class.java)

    val simpanJawaban: saveJawaban
        get() = RetrofitClient.getClient(BASE)!!.create(saveJawaban::class.java)

    val uploadImg: upImg
        get() = RetrofitClient.getClient(BASE)!!.create(upImg::class.java)

    val getGambar: getImg
        get() = RetrofitClient.getClient(BASE)!!.create(getImg::class.java)

    val getInfoProfile: getProfile
        get() = RetrofitClient.getClient(BASE)!!.create(getProfile::class.java)

    val getRound: getRonde
        get() = RetrofitClient.getClient(BASE)!!.create(getRonde::class.java)

    val getRoundId: getRondeID
        get() = RetrofitClient.getClient(BASE)!!.create(getRondeID::class.java)

    val getQrcode: getQr
        get() = RetrofitClient.getClient(BASE)!!.create(getQr::class.java)
}