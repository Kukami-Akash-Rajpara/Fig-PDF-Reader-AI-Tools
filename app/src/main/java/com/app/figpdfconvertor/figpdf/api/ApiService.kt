package com.app.figpdfconvertor.figpdf.api

import com.app.figpdfconvertor.figpdf.model.PdfSummarizer.AnalyzeResponse
import com.app.figpdfconvertor.figpdf.model.PdfSummarizer.AnswerResponse
import com.app.figpdfconvertor.figpdf.model.PdfSummarizer.UploadResponse
import com.app.figpdfconvertor.figpdf.model.ResumeAnalyzer.ResumeAnalyzerHiring
import com.app.figpdfconvertor.figpdf.model.SkillTestResponse
import com.app.figpdfconvertor.figpdf.model.SubmitAnswerRequest
import com.app.figpdfconvertor.figpdf.model.SubmitAnswerResponse
import com.app.figpdfconvertor.figpdf.model.ocr.GetResponse
import com.app.figpdfconvertor.figpdf.model.ocr.OcrResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @Multipart
    @POST("resume/api/v1/interview/generate-skilltest-questions/")
    suspend fun generateQuestions(
        @Query("app_version") appVersion: Int,   // query param
        @Part resume: MultipartBody.Part,
        @Part("position_level") positionLevel: RequestBody,
        @Part("question_difficulty") difficulty: RequestBody,
        @Part("num_questions") numQuestions: RequestBody,
    ): Response<SkillTestResponse>


    @POST("resume/api/v1/interview/submit-candidte-answer/")
    suspend fun submitCandidateAnswer(
        @Query("session_id") sessionId: String,
        @Query("app_version") appVersion: Int,
        @Body request: SubmitAnswerRequest
    ): Response<SubmitAnswerResponse>

    @GET("resume/api/v1/report/download-interview-report/")
    suspend fun downloadInterviewReport(
        @Query("session_id") sessionId: String,
        @Query("app_version") appVersion: Int
    ): Response<ResponseBody>


    /* Pdf Summarize */

    @Multipart
    @POST("summarizer/upload/")
    fun uploadPdf(
        @Query("app_version") appVersion: Int,
        @Part file: MultipartBody.Part
    ): Call<UploadResponse>

    @FormUrlEncoded
    @POST("summarizer/get_pdf_summary/")
    fun analyzeDocument(
        @Query("app_version") appVersion: Int,
        @Field("session_id") sessionId: String,
        @Field("target_language") targetLanguage: String
    ): Call<AnalyzeResponse>

    @FormUrlEncoded
    @POST("summarizer/get_answer_question/")
    fun answerQuestion(
        @Query("session_id") sessionId: String,
        @Query("app_version") appVersion: Int,
        @Field("question") question: String,
        @Field("target_language") targetLanguage: String
    ): Call<AnswerResponse>


    /* Resume Analyzer */

    @Multipart
    @POST("resume/get_hiring/")
    fun getHiring(
        @Query("app_version") appVersion: Int,
        @Part resume: MultipartBody.Part?,
        @Part("jd_text") jdText: RequestBody?
    ): Call<ResumeAnalyzerHiring?>?

    @Multipart
    @POST("resume/get_candidate/")
    fun getCandidates(
        @Query("app_version") appVersion: Int,
        @Part resume: MultipartBody.Part?
    ): Call<ResumeAnalyzerHiring?>?


    //OCR

    @Multipart
    @POST("ocr/upload-image/")
    fun uploadOcrImage(
        @Part file: MultipartBody.Part?,
        @Query("app_version") appVersion: Int
    ): Call<OcrResponse?>?

    @GET("ocr/job/{job_id}")
    fun getJobStatus(@Path("job_id") jobId: String?): Call<GetResponse?>?
}

