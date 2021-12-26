package dev.abarmin.velosiped.task10;

@Controller
public class EndpointHandler {
    @RequestMapping(path = "/sum-post", method = HttpMethod.POST)
    public Response calculateSum(@RequestBody Request request) {
        return new Response(request.getArg1() + request.getArg2());
    }

    @RequestMapping(path = "/sum", method = HttpMethod.GET)
    public int calculateSumFromQueryParams(@QueryParameter(value = "a") int a, @QueryParameter(value = "b")int b) {
        return a + b;
    }
}
