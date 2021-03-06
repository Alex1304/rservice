package com.github.alex1304.rdi.config;

import com.github.alex1304.rdi.RdiException;
import reactor.core.Exceptions;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

public class SetterMethod {

    private final Class<?> owner;
    private final String setterName;
    private final Class<?> returnType;
    private final Injectable param;
    private final MethodHandle methodHandle;

    SetterMethod(Class<?> owner, String setterName, Class<?> returnType, Injectable param) {
        this.owner = owner;
        this.setterName = setterName;
        this.returnType = returnType;
        this.param = param;
        this.methodHandle = prepareMethodHandle();
    }

    public Injectable getInjectableParameter() {
        return param;
    }

    public void invoke(Object instance, Object arg) {
        try {
            methodHandle.invoke(instance, arg);
        } catch (Throwable t) {
            throw Exceptions.propagate(t);
        }
    }

    public void invoke(Object instance) {
        try {
            methodHandle.invoke(instance);
        } catch (Throwable t) {
            throw Exceptions.propagate(t);
        }
    }

    private MethodHandle prepareMethodHandle() {
        try {
            MethodHandle mh = MethodHandles.publicLookup()
                    .findVirtual(owner, setterName, MethodType.methodType(returnType, param.getType()));
            if (param.getValue().isPresent()) {
                mh = MethodHandles.insertArguments(mh, 1, param.getValue().get());
            }
            return mh.asType(mh.type().generic());
        } catch (ReflectiveOperationException e) {
            throw new RdiException("Error when acquiring setter method '"
                    + setterName + "(" + param.getType().getName() + ")' for class " + owner.getName(), e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, param.getType(), setterName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SetterMethod))
            return false;
        SetterMethod other = (SetterMethod) obj;
        return Objects.equals(owner, other.owner) && Objects.equals(param.getType(), other.param.getType())
                && Objects.equals(setterName, other.setterName);
    }

    @Override
    public String toString() {
        return "SetterMethod{owner=" + owner + ", setterName=" + setterName + ", returnType=" + returnType + ", param="
                + param + ", methodHandle=" + methodHandle + "}";
    }
}
