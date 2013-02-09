package dongfang.xsltools.web;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dongfang.xsltools.context.InteractiveRequestResponseBean;

/**
 * This could be dumped. Not used anywhere.
 * @author dongfang
 *
 */
public class XMLServlet extends HttpServlet {

  /**
	 * 
	 */
	private static final long serialVersionUID = 5799756403886796156L;

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String key = request.getParameter("key");

    InteractiveRequestResponseBean validator = null; 

    HttpSession session = request.getSession();

    if (session != null) {
      if (validator == null)
        validator = (InteractiveRequestResponseBean)session.getAttribute("validator");

      response.setBufferSize(10 * 1024 * 1024); // Improve on this!!! How to make it auto-flush??
      response.setContentType("application/xml");
      
      //OutputStream os = response.getOutputStream();
      //validator.getGeneratedXMLResourceAsString(key, os);
    } else
      System.err.println("Null session in ImageConverterServlet!");
  }
}
