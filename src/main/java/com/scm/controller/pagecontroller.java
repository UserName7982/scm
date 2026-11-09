package com.scm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scm.Services.services;
import com.scm.Userform.userform;
import com.scm.entity.User;
import com.scm.helper.Message;
import com.scm.helper.MessageType;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;




@Controller
public class pagecontroller {

    @Autowired
    private services userServices;

    @RequestMapping("/")
    public String home(Model model){
        model.addAttribute("name", "Fuction");
        return "index";
    }
    
    @RequestMapping("/about")
    public String abouts(){
        return"about";
       }

       @GetMapping("/login")
       public String logins() {
         
           
           return new String("login");
       }
      

    @RequestMapping("/services")
       public String service(){
           return"services";
          }

     @RequestMapping("/register")
         public String register(Model model){
            userform userform=new userform();
            userform.setName("");
            model.addAttribute("userform",userform);
          return"register";
             }
    
    @RequestMapping("/do_register")
    public String processregister(@Valid @ModelAttribute userform user,BindingResult rBindingResult,HttpSession session){
        System.out.println(user);
        if (rBindingResult.hasErrors()) {
            return "register";
        }else{

        User user1=new User();
        user1.setName(user.getName());
        user1.setEmail(user.getEmail());
        user1.setPassword(user.getPassword());
        user1.setPhoneno(user.getPhoneno());
        user1.setAbout(user.getAbout());
        user1.setEmailverified("true");
        user1.setEnabled(true);
        userServices.saveUser(user1);

        Message message=Message.builder().content("Registration Successfull").messageType(MessageType.green).build();

        session.setAttribute("message", message);
        
        return "redirect:/register";
    }

}
}

