package com.project.collegequora.controllers;

import java.util.Objects;

import javax.mail.internet.MimeMessage;

import com.project.collegequora.models.SystemUser;
import com.project.collegequora.repository.SubjectRepository;
import com.project.collegequora.repository.SystemUserRepository;
import com.project.collegequora.response.JWTResponseData;
import com.project.collegequora.response.Response;
import com.project.collegequora.security.JwtTokenUtil;
import com.project.collegequora.service.SystemUserDetailsService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web")
public class WebController 

{
    @Autowired
	SubjectRepository subjectRepository;
	@Autowired
	SystemUserRepository systemUserRepository;

	@Autowired
    private JavaMailSender javaMailSender;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
    @Autowired
    PasswordEncoder passwordEncoder;
	@Autowired
	private SystemUserDetailsService systemUserDetailsService;

	@Autowired
	private UserDetailsService userDetailsService;

	@PostMapping("/register")
	public Response saveUser(@RequestBody SystemUser user) 
	{
		if(systemUserRepository.existsByEmail(user.getEmail())){
			return new Response(400,"already a user",null);
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		SendVerifyMail(user.getName(),user.getEmail(),121);
		SystemUser newUser = systemUserRepository.save(user);
		if (newUser == null)
			return new Response(400,"already a user",null);
		else
            return  new Response(200, "registration successfull",subjectRepository.findAllByDeptId(user.getDeptId()));
	}


	@PostMapping("/login")
	public ResponseEntity login(@RequestBody SystemUser user) 
	{
		try {

			SystemUser newUser = systemUserRepository.findByEmail(user.getEmail());
		
			 if(newUser.isActive()){
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
			String datais=systemUserDetailsService.getuserEmail(user);
		
			final String token = jwtTokenUtil.generateToken(newUser);

			return ResponseEntity.ok(new JWTResponseData(true, token, "Login Successfully",datais));
			//subjectRepository.findAllByDeptId(user.getDeptId())));
		 }else{
			return ResponseEntity.ok(new JWTResponseData(false, "", "verify Email!!",""));
		}
		}catch (DisabledException e) {
			return ResponseEntity.ok(new JWTResponseData(false, "", "User Disabled !",","));
		} catch (BadCredentialsException e) {
			return ResponseEntity.ok(new JWTResponseData(false, "", "Invalid User !",","));
		}catch (Exception e) {
			return ResponseEntity.ok(new JWTResponseData(false, "", "Invalid User !",","));
		}
	}
	
	


	
	private boolean SendVerifyMail(String name,String email,int otp) 
	{
		try {
			SimpleMailMessage msg = new SimpleMailMessage();
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
	        messageHelper.setFrom("soundarya.17ec095@cambridge.edu.in");
	        messageHelper.setTo(email);
	        messageHelper.setSubject("Verification Mail from PatientWeb");
	        messageHelper.setText("<b><a href=http://localhost:8082/web/vrifcation/"+email+">click </a></b>", true);
	        javaMailSender.send(mimeMessage);
			return true;
		}catch(Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}
	private boolean SendVerifyMail(String name,String email,String hospitalid) 
	{
		try {
			SimpleMailMessage msg = new SimpleMailMessage();
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
	        messageHelper.setFrom("prajwalsr1999@gmail.com");
	        messageHelper.setTo(email);
	        messageHelper.setSubject("Verification Mail from PatientWeb");
	        messageHelper.setText("<b><a href=http://localhost:8080/Hospital/vrifcation/"+email+">click </a></b>", true);
	        javaMailSender.send(mimeMessage);
			return true;
		}catch(Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}

    @GetMapping("/vrifcation/{email}")
    public String vrification(@PathVariable String email)
    {
        SystemUser userdata=systemUserRepository.findByEmail(email);
        System.out.println(userdata);
        userdata.setActive(true);
        System.out.println(userdata);
        systemUserRepository.save(userdata);
        return "<h1>"+email+"</h1>";
    }

	
}
