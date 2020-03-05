package com.example.demo;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TwittRepository  twittRepository;

    @Autowired
    CloudinaryConfig cloudc;

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @PostMapping("/register")
    public String processRegistrationPage(@Valid @ModelAttribute("user") User user, BindingResult result,
                           Model model) {
                      model.addAttribute("user", user);
                      if (result.hasErrors())
                      {
                          return "registration";
                      }
                      else
                      {
                          userService.saveUser(user);
                          model.addAttribute("message", "User Account Created");
                      }
                      return "index";
    }


    @RequestMapping("/")
    public String index(Model model){
        model.addAttribute("twitts",  twittRepository.findAll());

        model.addAttribute("user", userRepository.findAll());

        return "index";
    }

    @RequestMapping("/login")
    public String login(){
        return "login";
    }

    ////////////////////////////////BULLHORN///////////////////////////
    @RequestMapping("/list")
    public String listTwitts(Model model, Principal principal){
        model.addAttribute("twitts",  twittRepository.findAll());
        model.addAttribute("user_id",userRepository.findByUsername(principal.getName()).getId());
        return "list";
    }

    @GetMapping("/add")
    public String  twittForm(Model model, Principal principal){
        model.addAttribute("twitt", new  Twitt());
        model.addAttribute("user_id",userRepository.findByUsername(principal.getName()).getId());
        return "twittform";
    }

    @PostMapping("/add")
    public String processTwitt(@Valid @ModelAttribute Twitt twitt, BindingResult result,
                              @RequestParam("file") MultipartFile file,
                             @RequestParam("twitt_user_id") long twitt_user_id, Principal principal) {
        if (result.hasErrors()) {
            return "twittform";
        }

        if (file.isEmpty()) {


        } else {
            try {
                Map uploadResult = cloudc.upload(file.getBytes(),
                        ObjectUtils.asMap("resourcetype", "auto"));
                twitt.setImage(uploadResult.get("url").toString());
                twittRepository.save(twitt);
            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/add";
            }
        }
//        carRepository.save(car);
//        return "redirect:/index1";



        User user = userRepository.findById(twitt_user_id).get();
        twitt.setUser(user);


        twittRepository.save(twitt);
        return "redirect:/";

    }



    @RequestMapping("/detail/{id}")
    public String showTwitt(@PathVariable("id") long id, Model model)
    {
        model.addAttribute("twitt", twittRepository.findById(id).get());
        return "show";
    }

    @RequestMapping("/update/{id}")
    public String updateTwitt(@PathVariable("id") long id,
                            Model model, Principal principal ){
        model.addAttribute("twitt", twittRepository.findById(id).get());
        model.addAttribute("user_id", userRepository.findByUsername(principal.getName()).getId());
        Twitt twitt = twittRepository.findById(id).get();
        model.addAttribute("twitt", twitt);

        return "twittform";
    }

    @RequestMapping("/delete/{id}")
    public String deleteTwitt(@PathVariable("id") long id){
        twittRepository.deleteById(id);
        return "redirect:/list";
    }





    /////////////////////////////////////////////////////////

//    @RequestMapping("/secure")
//    public String admin(){
//        return "secure";
//    }


    @RequestMapping("/secure")
    public String secure(Principal principal, Model model){
        String username = principal.getName();
        model.addAttribute("user", userRepository.findByUsername(username));
        return "secure";
    }



}
