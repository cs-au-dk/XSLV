package dongfang.xsltools.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.dom4j.Document;

import dongfang.xsltools.context.InteractiveRequestResponseBean;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.Dom4jUtil;

public class StylesheetServlet extends HttpServlet {

  /**
	 * 
	 */
	private static final long serialVersionUID = -549893120114388585L;

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String key = request.getParameter("module");
    String version = request.getParameter("version");
    
    InteractiveRequestResponseBean validator = null;
    
    HttpSession session = request.getSession();

    if (session != null) {
      if (validator == null)
        validator = (InteractiveRequestResponseBean)session.getAttribute("validator");
      
      response.setBufferSize(10 * 1024 * 1024); // Improve on this!!! How to make it auto-flush??
      response.setContentType("application/xml");
      
      Stylesheet ss = (Stylesheet)validator.getGeneratedResources().get(version);
      
      StylesheetModule sm = (key == null) ? ss : ss.getModule(key);
      
      OutputStream os = response.getOutputStream();
      
      Document d = sm.getDocument(DiagnosticsConfiguration.SIMPLIFIED);
      Dom4jUtil.debugPrettyPrint(d, os);
      
      // validator.getGeneratedXMLResourceAsString(key, os);
    } else
      System.err.println("Null session in ImageConverterServlet!");
  }
}
