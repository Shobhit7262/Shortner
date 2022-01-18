package com.yourl.controller;

import com.google.common.hash.Hashing;
import com.yourl.controller.dto.ShortenUrlRequest;
import com.yourl.service.IUrlStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


@Controller
public class UrlController {
    @Autowired
    private IUrlStoreService urlStoreService;

//    Intial response as a html webpage showing UI
    @RequestMapping(value="/", method=RequestMethod.GET)
    public String showForm(ShortenUrlRequest request) {
        return "shortener";
    }


//    Error on passing paramater in the url itself
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public void redirectToUrl(@PathVariable String id, HttpServletResponse resp) throws Exception {
        final String url = urlStoreService.findUrlById(id);
        if (url != null) {
            resp.addHeader("Location", url);
            resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

//    post call to short the given url
    @RequestMapping(value="/", method = RequestMethod.POST)
    public ModelAndView shortenUrl(HttpServletRequest httpRequest,
                                   @Valid ShortenUrlRequest request,
                                   BindingResult bindingResult) {
        String url = request.getUrl();
        if (!isUrlValid(url)) {
            bindingResult.addError(new ObjectError("url", "Invalid url format: " + url));
        }

        ModelAndView modelAndView = new ModelAndView("shortener");
        String id1 = null;
        if (!bindingResult.hasErrors()) {
            final String id = Hashing.murmur3_32()
                    .hashString(url, StandardCharsets.UTF_8).toString();
            urlStoreService.storeUrl(id, url);
            String requestUrl = httpRequest.getRequestURL().toString();
            String prefix = requestUrl.substring(0, requestUrl.indexOf(httpRequest.getRequestURI(),
                    "http://".length()));
            id1 = id;

            modelAndView.addObject("shortenedUrl", prefix + "/" + id);
        }
        System.out.println(id1 + urlStoreService.findUrlById(id1) );

        return modelAndView;
    }

//    Checking is the url is valid or not
    private boolean isUrlValid(String url) {
        boolean valid = true;
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            valid = false;
        }
        return valid;
    }
}
