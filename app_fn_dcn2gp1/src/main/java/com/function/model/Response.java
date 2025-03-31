package com.function.model;

/**
 * Clase genérica para estandarizar las respuestas de la API.
 * @param <T> Tipo de dato que contendrá el objeto en la respuesta
 */
public class Response<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;

    // Constructor para respuestas exitosas con datos
    public Response(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Constructor para respuestas de error
    public Response(boolean success, String message, String error) {
        this.success = success;
        this.message = message;
        this.error = error;
    }

    // Método estático para crear respuestas exitosas
    public static <T> Response<T> success(String message, T data) {
        return new Response<>(true, message, data);
    }

    // Método estático para crear respuestas de error
    public static <T> Response<T> error(String message, String error) {
        return new Response<>(false, message, error);
    }

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}