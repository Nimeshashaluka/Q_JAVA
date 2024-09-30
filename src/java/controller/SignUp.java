package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.User_Status;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.HibernateUtil;
import model.Validation;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@MultipartConfig
@WebServlet(name = "SignUp", urlPatterns = {"/SignUp"})
public class SignUp extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

//        JsonObject requestJson = gson.fromJson(request.getReader(),JsonObject.class);
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String mobile = request.getParameter("mobile");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        Part avatarImage = request.getPart("avatarImage");

        if (firstName.isEmpty()) {
            responseJson.addProperty("message", "Please Fill Your First Name");
        } else if (lastName.isEmpty()) {
            responseJson.addProperty("message", "Please Fill Your Last Name");
        } else if (mobile.isEmpty()) {
            responseJson.addProperty("message", "Please Fill Your Mobile Number");
        } else if (!Validation.isMobile(mobile)) {
            responseJson.addProperty("message", "Invalid Mobile Number");
        } else if (email.isEmpty()) {
            responseJson.addProperty("message", "Please Fill Your Email");
        } else if (!Validation.isEmailValid(email)) {
            responseJson.addProperty("message", "Invalid Email");
        } else if (password.isEmpty()) {
            responseJson.addProperty("message", "Please Fill Your Password");
        } else if (!Validation.isPasswordValid(password)) {
            responseJson.addProperty("message", "Password must include atleast one uppercase letter, a number, a special character and be 8 characters long");
        } else if (avatarImage == null) {
            responseJson.addProperty("message", "Please Select Your Profile Image");

        } else {

            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria1 = session.createCriteria(User.class);
            criteria1.add(Restrictions.eq("mobile", mobile));

            if (!criteria1.list().isEmpty()) {
                responseJson.addProperty("message", "This Number Already Added!");

            } else {
                User user = new User();
                user.setFirst_name(firstName);
                user.setLast_name(lastName);
                user.setMobile(mobile);
                user.setEmail(email);
                user.setPassword(password);
                user.setJoin_date_time(new Date());

                //status 2 = offline
                User_Status user_Status = (User_Status) session.get(User_Status.class, 2);
                user.setUser_Status(user_Status);

                session.save(user);
                session.beginTransaction().commit();

                if (avatarImage != null) {
                    //select image

                    String serverPath = request.getServletContext().getRealPath("");
                    String avatarDirPath = serverPath + File.separator + "AvatarImages";
                    File avatarDir = new File(avatarDirPath);

// Ensure the directory exists
                    if (!avatarDir.exists()) {
                        avatarDir.mkdirs();
// Create the directory if it doesn't exist
                    }

                    String avatarImagePath = avatarDirPath + File.separator + mobile + ".png";
                    System.out.println(avatarImagePath);
                    System.out.println("ok");

                    File file = new File(avatarImagePath);
                    Files.copy(avatarImage.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

                }

                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "Registration Success!");

            }

            session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));

    }

}
