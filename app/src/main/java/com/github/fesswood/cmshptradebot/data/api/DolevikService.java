package com.github.fesswood.cmshptradebot.data.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by fesswood on 12.06.16.
 */
public interface DolevikService {
    @GET(RestConst.api.CMSHP)
    Call<ResponseBody> getCmshpBody();
}
