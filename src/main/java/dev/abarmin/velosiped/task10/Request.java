package dev.abarmin.velosiped.task10;

import java.util.Objects;

/**
 * DTO class for mapping incoming requests.
 */
public class Request {
    private int arg1;
    private int arg2;

    public Request() {
    }

    public Request(int arg1, int arg2) {
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public int getArg1() {
        return arg1;
    }

    public void setArg1(int arg1) {
        this.arg1 = arg1;
    }

    public int getArg2() {
        return arg2;
    }

    public void setArg2(int arg2) {
        this.arg2 = arg2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return arg1 == request.arg1 && arg2 == request.arg2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(arg1, arg2);
    }

    @Override
    public String toString() {
        return "Request{" +
                "arg1=" + arg1 +
                ", arg2=" + arg2 +
                '}';
    }
}