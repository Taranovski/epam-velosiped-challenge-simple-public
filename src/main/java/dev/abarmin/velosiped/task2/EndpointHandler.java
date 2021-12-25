package dev.abarmin.velosiped.task2;

public class EndpointHandler {
    public Response calculateSum(Request request) {
        return new Response(request.getArg1() + request.getArg2());
    }
}
