package dev.abarmin.velosiped.task9;

@Controller
public class EndpointHandler {
    @RequestMapping(path = "/sum-post", method = HttpMethod.POST)
    public Response calculateSum(@RequestBody Request request) {
        return new Response(request.getArg1() + request.getArg2());
    }
}
