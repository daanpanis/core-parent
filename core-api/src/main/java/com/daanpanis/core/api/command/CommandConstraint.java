package com.daanpanis.core.api.command;

import java.lang.annotation.Annotation;

public interface CommandConstraint<T, A extends Annotation> {

    void testConstraint(T value, A annotation);

}
