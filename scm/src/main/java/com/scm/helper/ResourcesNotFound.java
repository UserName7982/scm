package com.scm.helper;

public class ResourcesNotFound extends RuntimeException{

    public ResourcesNotFound(String message){
        super(message);
    }
    public ResourcesNotFound(){
        super("Resource not found");
    }
}
