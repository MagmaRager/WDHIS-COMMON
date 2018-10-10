package com.wdhis.common.interceptor;

import com.wdhis.common.interceptor.entity.JwtToken;
import com.wdhis.common.util.CookieUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ALAN on 2018/8/29.
 */
public class CookieInterceptor implements HandlerInterceptor {

    private static final String COOKIE_NAME = "HIS_TOKEN";  //cookie名称

    private List<String> urls;  //拦截url列表

    private List<String> urlse; //拦截url排除列表

    private List<String> urllogin;  //登录url列表

    private String key;     //DES加密密码
                            //JWT密钥

    private int vdTime; //过期时间

    public CookieInterceptor(String key, List<String> ul1, List<String> ul2, List<String> ullogin, int validateTime) {
        this.key = key;
        this.urls = ul1;
        this.urlse = ul2;
        this.urllogin = ullogin;
        this.vdTime = validateTime;
    }

    /**
     * Intercept the execution of a handler. Called after HandlerMapping determined
     * an appropriate handler object, but before HandlerAdapter invokes the handler.
     * <p>DispatcherServlet processes a handler in an execution chain, consisting
     * of any number of interceptors, with the handler itself at the end.
     * With this method, each interceptor can decide to abort the execution chain,
     * typically sending a HTTP error or writing a custom response.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  chosen handler to execute, for type and/or instance evaluation
     * @return {@code true} if the execution chain should proceed with the
     * next interceptor or the handler itself. Else, DispatcherServlet assumes
     * that this interceptor has already dealt with the response itself.
     * @throws Exception in case of errors
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("preHandle");

        boolean succeeded = false;
        String s = CookieUtil.getCookie(request, COOKIE_NAME);
        String url = request.getRequestURI();
        if(this.urlInList(url, urls) && !this.urlInList(url, urlse)){   //拦截
            if(s != null && !this.urlInList(url, urllogin)) { //不为空，且不是登录页面
                JwtToken jt = this.getJwtInfo(s);
                String ip = getIpAddress(request);
                if (!jt.getIp().equals(ip)) {
                    System.out.println("ip不一致");
                }
                else {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date currentdate = new Date();
                    String exp = formatter.format(jt.getExpiration());
                    ParsePosition pos = new ParsePosition(0);
                    Date strtodate = formatter.parse(exp, pos);
                    if (currentdate.getTime() > strtodate.getTime()) {
                        System.out.println("该cookie已过期");
                    } else {
                        String snew = this.createJwtString(jt);
                        CookieUtil.writeCookie(response, COOKIE_NAME, snew);
                        succeeded = true;
                    }
                }
//                System.out.println("Get Cookie: " + s);
//                List<String> list = new ArrayList<>();
//                try {
//                    String decryResult = DesUtil.decryptor(s, this.key);    //解密
//                    System.out.println("解密后：" + new String(decryResult));
//
//                    //验证是否具有权限，若无则拦截
//                    list = Arrays.asList(decryResult.split(";"));
//                    int i = 0;
//                    String dateUpdated = "";
//                    for(String parmstr : list) {
//                        if(i == 0) {    //  ip判断一致
//                            String ip = getIpAddress(request);
//                            if(!parmstr.equals(ip)) {
//                                System.out.println("ip不一致");
//                                break;
//                            }
//                        }
//                        if(i == 3) {    //  时间判断是否过期
//                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                            Date currentdate = new Date();
//                            ParsePosition pos = new ParsePosition(0);
//                            Date strtodate = formatter.parse(parmstr, pos);
//                            if (currentdate.getTime() - strtodate.getTime() > vdTime) {
//                                System.out.println("该cookie已过期");
//                                break;
//                            } else {
//                                dateUpdated = formatter.format(currentdate);
//                            }
//                        }
//                        i++;
//                    }
//                    String str = list.get(0) + ";" + list.get(1) + ";" + list.get(2) + ";" + dateUpdated; //替换cookie时间值为当前时间
//                    String result = DesUtil.encrypt(str, this.key);
//                    System.out.println("加密后：" + result);
//                    CookieUtil.writeCookie(response, COOKIE_NAME, result);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                request.setAttribute("branchId", list.get(1));
//                request.setAttribute("empId", list.get(2));
            }
        }
        else {
            succeeded = true;
        }
        stopWatch.stop();
        //System.out.println("preHandle: " + stopWatch.getTotalTimeMillis());
        return succeeded;
    }

    /**
     * Intercept the execution of a handler. Called after HandlerAdapter actually
     * invoked the handler, but before the DispatcherServlet renders the view.
     * Can expose additional model objects to the view via the given ModelAndView.
     * <p>DispatcherServlet processes a handler in an execution chain, consisting
     * of any number of interceptors, with the handler itself at the end.
     * With this method, each interceptor can post-process an execution,
     * getting applied in inverse order of the execution chain.
     *
     * @param request      current HTTP request
     * @param response     current HTTP response
     * @param handler      handler that started async
     *                     execution, for type and/or instance examination
     * @param modelAndView the {@code ModelAndView} that the handler returned
     *                     (can also be {@code null})
     * @throws Exception in case of errors
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //System.out.println("-postHandle-");
    }

    /**
     * Callback after completion of request processing, that is, after rendering
     * the view. Will be called on any outcome of handler execution, thus allows
     * for proper resource cleanup.
     * <p>Note: Will only be called if this interceptor's {@code preHandle}
     * method has successfully completed and returned {@code true}!
     * <p>As with the {@code postHandle} method, the method will be invoked on each
     * interceptor in the chain in reverse order, so the first interceptor will be
     * the last to be invoked.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  handler that started async
     *                 execution, for type and/or instance examination
     * @param ex       exception thrown on handler execution, if any
     * @throws Exception in case of errors
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        String url = request.getRequestURI();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = "";
        Date currentdate = new Date();
        String dateUpdated = formatter.format(currentdate);
        if(this.urlInList(url, urllogin)) {  //为登录处理

            String branchId = "01";
            String empId = "999";
            String ip = getIpAddress(request);
            JwtToken jt = new JwtToken();
            jt.setBranchId(branchId);
            jt.setEmpId(empId);
            jt.setIp(ip);

            String jwtToken = this.createJwtString(jt);
            str = jwtToken;

            CookieUtil.writeCookie(response, COOKIE_NAME, str);
        }
        else {
            Object bid = request.getAttribute("branchId");
            if(bid != null) {
                //lastModified时间刷新
                String s = CookieUtil.getCookie(request, COOKIE_NAME);
                JwtToken jt = this.getJwtInfo(s);
                String snew = this.createJwtString(jt);
                CookieUtil.writeCookie(response, COOKIE_NAME, snew);
//                String decryResult = DesUtil.decryptor(s, this.key);
//                int idx = decryResult.lastIndexOf(';');
//                str = decryResult.substring(0, idx + 1) + dateUpdated;
            }
            else return;
        }
        //String result = DesUtil.encrypt(str, this.key);
        //System.out.println("加密后：" + result);
        //CookieUtil.writeCookie(response, COOKIE_NAME, result);
    }

    private String createJwtString(JwtToken jt) {
        Date nowDate = new Date();
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject("Sub")
                .claim("brc", jt.getBranchId())
                .claim("emp", jt.getEmpId())
                .claim("ip", jt.getIp())
                .setIssuedAt(nowDate)
                .setExpiration(DateUtils.addSeconds(nowDate, vdTime))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    private JwtToken getJwtInfo(String jwt) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(jwt)
                .getBody();
        JwtToken jt = new JwtToken();
        jt.setBranchId(claims.get("brc").toString());
        jt.setEmpId(claims.get("emp").toString());
        jt.setIp(claims.get("ip").toString());
        jt.setExpiration(claims.getExpiration());

        return jt;
    }

    private boolean urlInList(String url, List<String> urls){
        for (String urlx: urls) {
            if(urlx.endsWith("/*")) {
                int length = urlx.length() - 2;
                if(url.substring(0, length).equals(urlx.substring(0, length))){
                    return true;
                }
            }
            else {
                if(url.equals(urlx)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ip != null && ip.length() != 0) {
            ip = ip.split(",")[0];
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
