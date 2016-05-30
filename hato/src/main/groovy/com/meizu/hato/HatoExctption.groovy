package com.meizu.hato

import org.gradle.api.GradleException

class HatoExctption extends GradleException {
    public HatoExctption(){}

    public HatoExctption(String message){
        super(message);
    }
    public HatoExctption(String message, Throwable throwable){
        super(message, throwable);
    }

}


