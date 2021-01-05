package todo.data.remote

import core.Api
import retrofit2.http.*
import todo.data.local.Participant

object ParticipantApi {
    interface Service {
        @GET("/api/participant")
        suspend fun find(): List<Participant>

        @GET("/api/participant/{id}")
        suspend fun read(@Path("id") participantId: Int): Participant;

        @Headers("Content-Type: application/json")
        @POST("/api/participant")
        suspend fun create(@Body participant: Participant): Participant

        @Headers("Content-Type: application/json")
        @PUT("/api/participant/{id}")
        suspend fun update(@Path("id") participantId: Int, @Body participant: Participant): Participant
    }

    val service: Service = Api.retrofit.create(Service::class.java)
}