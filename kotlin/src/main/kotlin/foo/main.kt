package foo

import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder
import com.amazonaws.services.apigateway.model.GetIntegrationRequest
import com.amazonaws.services.apigateway.model.GetResourcesRequest
import com.amazonaws.services.apigateway.model.GetRestApisRequest
import com.amazonaws.services.apigateway.model.NotFoundException
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

val methods = listOf("GET", "POST", "DELETE", "PUT", "PATCH")

fun main(args: Array<String>) {

    val data = mutableMapOf<String, MutableMap<String, MutableList<String>>>()

    val client = AmazonApiGatewayClientBuilder.defaultClient()

    client.getRestApis(GetRestApisRequest()).items.forEach { api ->
        val apiData = mutableMapOf<String, MutableList<String>>()
        val getResourcesRequest = GetResourcesRequest().apply {
            restApiId = api.id
            limit = 7777
        }
        client.getResources(getResourcesRequest).items.forEach { resource ->
            methods.forEach { method ->
                val getIntegrationRequest = GetIntegrationRequest().apply {
                    restApiId = api.id
                    resourceId = resource.id
                    httpMethod = method
                }
                try {
                    val integration = client.getIntegration(getIntegrationRequest)
                    if (integration.uri?.contains("lambda") == true) {
                        val lambdaFunction = integration.uri.split(':').last().split('/').first()
                        if (!apiData.containsKey(lambdaFunction)){
                            apiData[lambdaFunction] = mutableListOf()
                        }
                        val thing = "$method, ${resource.path}"
                        println("adding $thing")
                        apiData[lambdaFunction]?.add(thing)
                    }
                } catch (e: NotFoundException) {

                }
            }

        }
        data[api.name] = apiData
    }
    println(GsonBuilder().setPrettyPrinting().create().toJson(data))

}