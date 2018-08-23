package ex.aaronfae.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import ex.aaronfae.domain.Score;

public class ScoreUtil {

	public static Header login(String xh, String mm) {
		// 目标URL
		String loginUrl = "http://61.142.33.204/default4.aspx";

		// 这是最终的返回值，由cookieValue和cookieName拼接而成
		String cookieString = null;
		String cookieValue = null;
		String cookieName = null;

		// 这个CookieStore是给下面HttpClient对象用于execute后，保存Cookie的
		CookieStore cookieStore = new BasicCookieStore();

		// 这里不重用HttpClient和HttpGet的原因是
		// 因为重用了之后，不能每次都获取到新的cookie
		// TODO 后续需要找到重用之后还能获取到新的cookie的方法
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		HttpGet httpGet = new HttpGet(loginUrl);
		try {
			// 做一个循环，一定能拿到Cookie
			do {
				// TODO 确认不分配HttpResponse对象是否也要关闭
				httpClient.execute(httpGet);
				List<Cookie> cookies = cookieStore.getCookies();
				if (cookies.isEmpty()) {
					continue;
				}
				cookieValue = cookies.get(0).getValue();
				cookieName = cookies.get(0).getName();
				cookieString = cookieName + "=" + cookieValue;
			} while ("null=null".equals(cookieString) || null == cookieString);

			// TODO 确认是否要关闭
			httpClient.close();

			httpClient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(loginUrl);

			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			httpPost.setHeader("Cookie", cookieString);
			httpPost.setHeader("Host", "61.142.33.204");
			httpPost.setHeader("Origin", "http://61.142.33.204");
			httpPost.setHeader("Referer", "http://61.142.33.204/default4.aspx");
			ArrayList<NameValuePair> pairs = new ArrayList<>();
			NameValuePair e1 = new BasicNameValuePair("__VIEWSTATE",
					"dDwxMTE4MjQwNDc1Ozs+MzFt0h81g6NGHTq1L9P2NfWUGLA=");
			NameValuePair e2 = new BasicNameValuePair("TextBox1", xh);
			NameValuePair e3 = new BasicNameValuePair("TextBox2", mm);
			NameValuePair e4 = new BasicNameValuePair("RadioButtonList1", "学生");
			NameValuePair e5 = new BasicNameValuePair("Button1", " 登 录  ");
			pairs.add(e1);
			pairs.add(e2);
			pairs.add(e3);
			pairs.add(e4);
			pairs.add(e5);
			UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(pairs, Charset.forName("GB2312"));
			httpPost.setEntity(urlEncodedFormEntity);

			// TODO 确认不分配HttpResponse对象是否也要关闭
			httpClient.execute(httpPost);
			Header cookie = httpPost.getFirstHeader("Cookie");
			return cookie;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<Score> getSemesterScore(String url, Header cookie, String year, String semeste) {

		HttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader(cookie);

		// TODO 需要更改为，跟浏览器页面相一致的值
		httpPost.setHeader("Referer", "http://61.142.33.204");

		// 请求的Form Data
		ArrayList<NameValuePair> pairs = new ArrayList<>();
		// 这个__VIEWSTATE参数需要测试是否每个账号都是固定的
		// 通过用浏览器测试了一下，这里有两个值可以用，一个是第一次请求的值，一个是第二次及以后请求的值，这里先取短的（第一次请求的值）
		// NameValuePair e6 = new BasicNameValuePair("__VIEWSTATE",
		// "dDwtMTc4NTk2MTQyMDt0PHA8bDx4aDtzZmRjYms7ZHlieXNjajt6eGNqY3h4cztzdHJfeG54cWN4Oz47bDwyMDE1MTMxMDA5O1xlO1xlOzA7MTs+PjtsPGk8MT47PjtsPHQ8O2w8aTwxPjtpPDM+O2k8NT47aTw3PjtpPDk+O2k8MTE+O2k8MTM+O2k8MTU+O2k8MjQ+O2k8MjU+O2k8MjY+O2k8MzE+O2k8MzM+O2k8MzU+O2k8Mzc+O2k8Mzk+O2k8NDE+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPOWtpuWPt++8mjIwMTUxMzEwMDk7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOWnk+WQje+8muWQkem+memjnjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w85a2m6Zmi77ya6K6h566X5py657O7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzkuJPkuJrvvJo7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOi9r+S7tuW3peeoizs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w86KGM5pS/54+t77yaMTXova/ku7bmnKznp5ExMOePrTs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MjAxNTEwMDg7Pj47Pjs7Pjt0PHQ8O3Q8aTwxOT47QDxcZTsyMDAxLTIwMDI7MjAwMi0yMDAzOzIwMDMtMjAwNDsyMDA0LTIwMDU7MjAwNS0yMDA2OzIwMDYtMjAwNzsyMDA3LTIwMDg7MjAwOC0yMDA5OzIwMDktMjAxMDsyMDEwLTIwMTE7MjAxMS0yMDEyOzIwMTItMjAxMzsyMDEzLTIwMTQ7MjAxNC0yMDE1OzIwMTUtMjAxNjsyMDE2LTIwMTc7MjAxNy0yMDE4OzIwMTgtMjAxOTs+O0A8XGU7MjAwMS0yMDAyOzIwMDItMjAwMzsyMDAzLTIwMDQ7MjAwNC0yMDA1OzIwMDUtMjAwNjsyMDA2LTIwMDc7MjAwNy0yMDA4OzIwMDgtMjAwOTsyMDA5LTIwMTA7MjAxMC0yMDExOzIwMTEtMjAxMjsyMDEyLTIwMTM7MjAxMy0yMDE0OzIwMTQtMjAxNTsyMDE1LTIwMTY7MjAxNi0yMDE3OzIwMTctMjAxODsyMDE4LTIwMTk7Pj47Pjs7Pjt0PHA8O3A8bDxvbmNsaWNrOz47bDx3aW5kb3cucHJpbnQoKVw7Oz4+Pjs7Pjt0PHA8O3A8bDxvbmNsaWNrOz47bDx3aW5kb3cuY2xvc2UoKVw7Oz4+Pjs7Pjt0PHA8cDxsPFZpc2libGU7PjtsPG88dD47Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDIwMTctMjAxOOWtpuW5tOesrDLlrabmnJ/lrabkuaDmiJDnu6k7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOaJgOmAieWtpuWIhjIxLjMw77yb6I635b6X5a2m5YiGMjEuMzDvvJvph43kv67lrabliIbjgII7Pj47Pjs7Pjt0PDtsPGk8MD47aTwxPjtpPDM+O2k8NT47aTw3PjtpPDk+O2k8MTE+O2k8MTM+O2k8MTU+O2k8MTc+O2k8MTk+O2k8MjE+O2k8MjI+Oz47bDx0PEAwPHA8cDxsPFBhZ2VDb3VudDtfIUl0ZW1Db3VudDtfIURhdGFTb3VyY2VJdGVtQ291bnQ7RGF0YUtleXM7PjtsPGk8MT47aTwxMT47aTwxMT47bDw+Oz4+Oz47QDA8Ozs7Ozs7O0AwPHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Ozs7Oz47QDA8cDxsPFZpc2libGU7PjtsPG88dD47Pj47Ozs7PjtAMDxwPGw8VmlzaWJsZTs+O2w8bzxmPjs+Pjs7Ozs+O0AwPHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Ozs7Oz47QDA8cDxsPFZpc2libGU7PjtsPG88Zj47Pj47Ozs7PjtAMDxwPGw8VmlzaWJsZTs+O2w8bzxmPjs+Pjs7Ozs+Ozs7O0AwPHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Ozs7Oz47QDA8cDxsPFZpc2libGU7PjtsPG88dD47Pj47Ozs7Pjs7Ozs+Ozs7Ozs7Ozs7PjtsPGk8MD47PjtsPHQ8O2w8aTwxPjtpPDI+O2k8Mz47aTw0PjtpPDU+O2k8Nj47aTw3PjtpPDg+O2k8OT47aTwxMD47aTwxMT47PjtsPHQ8O2w8aTwwPjtpPDE+O2k8Mj47aTwzPjtpPDQ+O2k8NT47aTw2PjtpPDc+O2k8OD47aTw5PjtpPDEwPjtpPDExPjtpPDEyPjtpPDEzPjtpPDE0PjtpPDE1PjtpPDE2PjtpPDE3PjtpPDE4PjtpPDE5PjtpPDIwPjs+O2w8dDxwPHA8bDxUZXh0Oz47bDwyMDE3LTIwMTg7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDI7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDEwMTUwMzI7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPEoyRUXmoYbmnrbmioDmnK87Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOe7hOmAieivvi0yOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDw0Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwzLjk7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDkyOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDw4Nzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8ODk7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOiuoeeul+acuuezuzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+Oz4+O3Q8O2w8aTwwPjtpPDE+O2k8Mj47aTwzPjtpPDQ+O2k8NT47aTw2PjtpPDc+O2k8OD47aTw5PjtpPDEwPjtpPDExPjtpPDEyPjtpPDEzPjtpPDE0PjtpPDE1PjtpPDE2PjtpPDE3PjtpPDE4PjtpPDE5PjtpPDIwPjs+O2w8dDxwPHA8bDxUZXh0Oz47bDwyMDE3LTIwMTg7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDI7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDEwMTcwOTk7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPEoyRUXkvIHkuJrnuqflvIDlj5E7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOWunuiureivvjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8My4wOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwzLjM7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDc3Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDw5Mjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8ODM7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOiuoeeul+acuuezuzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+Oz4+O3Q8O2w8aTwwPjtpPDE+O2k8Mj47aTwzPjtpPDQ+O2k8NT47aTw2PjtpPDc+O2k8OD47aTw5PjtpPDEwPjtpPDExPjtpPDEyPjtpPDEzPjtpPDE0PjtpPDE1PjtpPDE2PjtpPDE3PjtpPDE4PjtpPDE5PjtpPDIwPjs+O2w8dDxwPHA8bDxUZXh0Oz47bDwyMDE3LTIwMTg7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDI7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDEwMTQwMzg7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPExpbnV45Y6f55CG5LiO5bqU55SoOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzkuJPkuJrlv4Xkv67or747Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDMuMDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Mzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8ODk7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDczLjU7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDgwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzorqHnrpfmnLrns7s7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0PjtpPDU+O2k8Nj47aTw3PjtpPDg+O2k8OT47aTwxMD47aTwxMT47aTwxMj47aTwxMz47aTwxND47aTwxNT47aTwxNj47aTwxNz47aTwxOD47aTwxOT47aTwyMD47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8MjAxNy0yMDE4Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwyOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwxMDE3MDk3Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDxMaW51eOe7vOWQiOWunumqjDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w85a6e6K6t6K++Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwxLjA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDMuNjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8OTI7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDc4Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDw4Njs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w86K6h566X5py657O7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47Pj47dDw7bDxpPDA+O2k8MT47aTwyPjtpPDM+O2k8ND47aTw1PjtpPDY+O2k8Nz47aTw4PjtpPDk+O2k8MTA+O2k8MTE+O2k8MTI+O2k8MTM+O2k8MTQ+O2k8MTU+O2k8MTY+O2k8MTc+O2k8MTg+O2k8MTk+O2k8MjA+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPDIwMTctMjAxODs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Mjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MTAxNjA2Mjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w86K6h566X5py657uE5oiQ5Y6f55CGOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzku7vpgInor747Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDEuNTs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8My41Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDw5Nzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Njc7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDg1Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47Pj47dDw7bDxpPDA+O2k8MT47aTwyPjtpPDM+O2k8ND47aTw1PjtpPDY+O2k8Nz47aTw4PjtpPDk+O2k8MTA+O2k8MTE+O2k8MTI+O2k8MTM+O2k8MTQ+O2k8MTU+O2k8MTY+O2k8MTc+O2k8MTg+O2k8MTk+O2k8MjA+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPDIwMTctMjAxODs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Mjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MTMxMjEyODs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w85Lq65bel5pm66IO95qaC6K66Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzlhazlhbHpgInkv67or747Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOiHqueEtuenkeWtpuexuzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Mi4wOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwxLjQ7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDY1Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDw2Mzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8NjQ7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOWfuuehgOmDqDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+Oz4+O3Q8O2w8aTwwPjtpPDE+O2k8Mj47aTwzPjtpPDQ+O2k8NT47aTw2PjtpPDc+O2k8OD47aTw5PjtpPDEwPjtpPDExPjtpPDEyPjtpPDEzPjtpPDE0PjtpPDE1PjtpPDE2PjtpPDE3PjtpPDE4PjtpPDE5PjtpPDIwPjs+O2w8dDxwPHA8bDxUZXh0Oz47bDwyMDE3LTIwMTg7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDI7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDEwMTQwMjI7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOi9r+S7tuW3peeoizs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w85LiT5Lia5b+F5L+u6K++Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwzOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwyLjI7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDcyOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDw3Mjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8NzI7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOiuoeeul+acuuezuzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+Oz4+O3Q8O2w8aTwwPjtpPDE+O2k8Mj47aTwzPjtpPDQ+O2k8NT47aTw2PjtpPDc+O2k8OD47aTw5PjtpPDEwPjtpPDExPjtpPDEyPjtpPDEzPjtpPDE0PjtpPDE1PjtpPDE2PjtpPDE3PjtpPDE4PjtpPDE5PjtpPDIwPjs+O2w8dDxwPHA8bDxUZXh0Oz47bDwyMDE3LTIwMTg7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDI7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDEwMTYwNjM7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOi9r+S7tuiuvuiuoeW4iOiupOivgTs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w85Lu76YCJ6K++Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwxLjU7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDEuODs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8NzMuNTs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8NjA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDY4Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47Pj47dDw7bDxpPDA+O2k8MT47aTwyPjtpPDM+O2k8ND47aTw1PjtpPDY+O2k8Nz47aTw4PjtpPDk+O2k8MTA+O2k8MTE+O2k8MTI+O2k8MTM+O2k8MTQ+O2k8MTU+O2k8MTY+O2k8MTc+O2k8MTg+O2k8MTk+O2k8MjA+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPDIwMTctMjAxODs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Mjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MTAxNjAwNTs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w86L2v5Lu26aG555uu566h55CGOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzku7vpgInor747Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDEuNTs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8NDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8OTA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDkwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDw5MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w86K6h566X5py657O7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47Pj47dDw7bDxpPDA+O2k8MT47aTwyPjtpPDM+O2k8ND47aTw1PjtpPDY+O2k8Nz47aTw4PjtpPDk+O2k8MTA+O2k8MTE+O2k8MTI+O2k8MTM+O2k8MTQ+O2k8MTU+O2k8MTY+O2k8MTc+O2k8MTg+O2k8MTk+O2k8MjA+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPDIwMTctMjAxODs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Mjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MzYxMTAwNTs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w85b2i5Yq/5LiO5pS/562WOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzlhazlhbHlv4Xkv67or747Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDAuNTs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8NC4xOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDw5MTs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8OTA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDkxOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzmgJ3mlL/pg6g7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0PjtpPDU+O2k8Nj47aTw3PjtpPDg+O2k8OT47aTwxMD47aTwxMT47aTwxMj47aTwxMz47aTwxND47aTwxNT47aTwxNj47aTwxNz47aTwxOD47aTwxOT47aTwyMD47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8MjAxNy0yMDE4Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwyOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwzMzExMDExOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzogYzkuJrlj5HlsZXkuI7lsLHkuJrmjIflr7w7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOWFrOWFseW/heS/ruivvjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MC4zOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwzOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDw4MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Jm5ic3BcOzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8ODA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDgwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwmbmJzcFw7Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzlm73pmYXmlZnogrLlrabpmaI7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjs+Pjs+Pjt0PEAwPHA8cDxsPFZpc2libGU7PjtsPG88Zj47Pj47Pjs7Ozs7Ozs7Ozs+Ozs+O3Q8QDA8cDxwPGw8UGFnZUNvdW50O18hSXRlbUNvdW50O18hRGF0YVNvdXJjZUl0ZW1Db3VudDtEYXRhS2V5czs+O2w8aTwxPjtpPDE4PjtpPDE4PjtsPD47Pj47Pjs7Ozs7Ozs7Ozs+O2w8aTwwPjs+O2w8dDw7bDxpPDE+O2k8Mj47aTwzPjtpPDQ+O2k8NT47aTw2PjtpPDc+O2k8OD47aTw5PjtpPDEwPjtpPDExPjtpPDEyPjtpPDEzPjtpPDE0PjtpPDE1PjtpPDE2PjtpPDE3PjtpPDE4Pjs+O2w8dDw7bDxpPDA+O2k8MT47aTwyPjtpPDM+O2k8ND47aTw1Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDwxOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzlv4Xkv67or747Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0PjtpPDU+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPDEwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzlhazlhbHlv4Xkv67or747Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDAuODA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0PjtpPDU+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPDExOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzln7rnoYDlv4Xkv67or747Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0PjtpPDU+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPDEyOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzkuJPkuJrlv4Xkv67or747Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDY7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0PjtpPDU+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPDEzOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzkuJPpobnlrp7ot7Xor747Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0PjtpPDU+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPDE0Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzlhbbku5blrp7ot7Xor747Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0PjtpPDU+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPDE1Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDznu4TpgInor74tMTs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+Oz4+O3Q8O2w8aTwwPjtpPDE+O2k8Mj47aTwzPjtpPDQ+O2k8NT47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8MTY7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOe7hOmAieivvi0yOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDw0Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47Pj47dDw7bDxpPDA+O2k8MT47aTwyPjtpPDM+O2k8ND47aTw1Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDwxNzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w857uE6YCJ6K++LTM7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0PjtpPDU+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPDE4Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDznu4TpgInor74tNDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+Oz4+O3Q8O2w8aTwwPjtpPDE+O2k8Mj47aTwzPjtpPDQ+O2k8NT47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8Mjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w86YCJ5L+u6K++Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47Pj47dDw7bDxpPDA+O2k8MT47aTwyPjtpPDM+O2k8ND47aTw1Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDwzOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzlhazlhbHpgInkv67or747Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDI7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0PjtpPDU+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPDQ7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOS4k+S4mumZkOmAieivvjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+Oz4+O3Q8O2w8aTwwPjtpPDE+O2k8Mj47aTwzPjtpPDQ+O2k8NT47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8NTs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w85qih5Z2X6K++Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47Pj47dDw7bDxpPDA+O2k8MT47aTwyPjtpPDM+O2k8ND47aTw1Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDw2Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzlrp7orq3or747Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDQ7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0PjtpPDU+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPDc7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOiAg+ivgeivvjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+Oz4+O3Q8O2w8aTwwPjtpPDE+O2k8Mj47aTwzPjtpPDQ+O2k8NT47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8OTs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w85Lu76YCJ6K++Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDw0LjUwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47Pj47dDw7bDxpPDA+O2k8MT47aTwyPjtpPDM+O2k8ND47aTw1Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDw5OTs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8XDxiXD7lkIjorqFcPC9iXD47Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDIxLjMwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwOz4+Oz47Oz47Pj47Pj47Pj47dDxAMDxwPHA8bDxQYWdlQ291bnQ7XyFJdGVtQ291bnQ7XyFEYXRhU291cmNlSXRlbUNvdW50O0RhdGFLZXlzOz47bDxpPDE+O2k8Nz47aTw3PjtsPD47Pj47Pjs7Ozs7Ozs7Ozs+O2w8aTwwPjs+O2w8dDw7bDxpPDE+O2k8Mj47aTwzPjtpPDQ+O2k8NT47aTw2PjtpPDc+Oz47bDx0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDzkurrmlofnpL7np5HjgIHoibrmnK/nsbs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDzoh6rnhLbnp5Hlrabnsbs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDI7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDzov5DliqjkvJHpl7Lnsbs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDzlupTnlKjvvIjogYzkuJrvvInmioDog73orq3nu4M7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDzkuJPkuJrln7rnoYDmi5PlsZXmqKHlnZc7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDzlhazlhbHpgInkv67or747Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDA7Pj47Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjtpPDI+O2k8Mz47aTw0Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDxcPGJcPuWQiOiuoVw8L2JcPjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8Mjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MDs+Pjs+Ozs+Oz4+Oz4+Oz4+O3Q8QDA8cDxwPGw8UGFnZUNvdW50O18hSXRlbUNvdW50O18hRGF0YVNvdXJjZUl0ZW1Db3VudDtEYXRhS2V5czs+O2w8aTwxPjtpPDE+O2k8MT47bDw+Oz4+Oz47Ozs7Ozs7Ozs7PjtsPGk8MD47PjtsPHQ8O2w8aTwxPjs+O2w8dDw7bDxpPDA+O2k8MT47aTwyPjtpPDM+O2k8ND47aTw1Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDzlkIjorqE7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOasoTs+Pjs+Ozs+Oz4+Oz4+Oz4+O3Q8QDA8cDxwPGw8VmlzaWJsZTtQYWdlQ291bnQ7XyFJdGVtQ291bnQ7XyFEYXRhU291cmNlSXRlbUNvdW50O0RhdGFLZXlzOz47bDxvPGY+O2k8MT47aTwwPjtpPDA+O2w8Pjs+Pjs+Ozs7Ozs7Ozs7Oz47Oz47dDxAMDxwPHA8bDxWaXNpYmxlO1BhZ2VDb3VudDtfIUl0ZW1Db3VudDtfIURhdGFTb3VyY2VJdGVtQ291bnQ7RGF0YUtleXM7PjtsPG88Zj47aTwxPjtpPDA+O2k8MD47bDw+Oz4+Oz47Ozs7Ozs7Ozs7Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOacrOS4k+S4muWFsTUyMOS6ujs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w85bmz5Z2H5a2m5YiG57up54K577yaMy4wMjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w85a2m5YiG57up54K55oC75ZKM77yaNjQuNDA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPFxlOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDx6Zjs+Pjs+Ozs+O3Q8cDxwPGw8SW1hZ2VVcmw7PjtsPC4vZXhjZWwvOTcwNTI3MC5qcGc7Pj47Pjs7Pjs+Pjt0PHA8cDxsPFRleHQ7VmlzaWJsZTs+O2w86Iez5LuK5pyq6YCa6L+H6K++56iL77yaO288dD47Pj47Pjs7Pjt0PEAwPHA8cDxsPFBhZ2VDb3VudDtfIUl0ZW1Db3VudDtfIURhdGFTb3VyY2VJdGVtQ291bnQ7RGF0YUtleXM7PjtsPGk8MT47aTwxPjtpPDE+O2w8Pjs+Pjs+O0AwPEAwPHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Ozs7Oz47QDA8cDxsPFZpc2libGU7PjtsPG88Zj47Pj47Ozs7Pjs7Ozs7Ozs+Ozs7Ozs7Ozs7PjtsPGk8MD47PjtsPHQ8O2w8aTwxPjs+O2w8dDw7bDxpPDA+O2k8MT47aTwyPjtpPDM+O2k8ND47aTw1PjtpPDY+O2k8Nz47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8MjAxNS0yMDE2Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwyOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwxMzEzMDAyOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzpq5jnrYnmlbDlraY7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDM7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOWfuuehgOW/heS/ruivvjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8NTM7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCZuYnNwXDs7Pj47Pjs7Pjs+Pjs+Pjs+Pjt0PEAwPDs7Ozs7Ozs7Ozs+Ozs+Oz4+Oz4+Oz7CCfd0zhyWmdbSXJtHtWe8AmxIEQ==");
		NameValuePair e6 = new BasicNameValuePair("__VIEWSTATE",
				"dDwtMTc4NTk2MTQyMDt0PHA8bDx4aDtzZmRjYms7ZHlieXNjajt6eGNqY3h4czs+O2w8MjAxNTEzMTAwOTtcZTtcZTswOz4+O2w8aTwxPjs+O2w8dDw7bDxpPDE+O2k8Mz47aTw1PjtpPDc+O2k8OT47aTwxMT47aTwxMz47aTwxNT47aTwyND47aTwyNT47aTwyNj47aTwzNT47aTwzOT47aTw0MT47PjtsPHQ8cDxwPGw8VGV4dDs+O2w85a2m5Y+377yaMjAxNTEzMTAwOTs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w85aeT5ZCN77ya5ZCR6b6Z6aOeOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzlrabpmaLvvJrorqHnrpfmnLrns7s7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOS4k+S4mu+8mjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w86L2v5Lu25bel56iLOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzooYzmlL/nj63vvJoxNei9r+S7tuacrOenkTEw54+tOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwyMDE1MTAwODs+Pjs+Ozs+O3Q8dDw7dDxpPDE5PjtAPFxlOzIwMDEtMjAwMjsyMDAyLTIwMDM7MjAwMy0yMDA0OzIwMDQtMjAwNTsyMDA1LTIwMDY7MjAwNi0yMDA3OzIwMDctMjAwODsyMDA4LTIwMDk7MjAwOS0yMDEwOzIwMTAtMjAxMTsyMDExLTIwMTI7MjAxMi0yMDEzOzIwMTMtMjAxNDsyMDE0LTIwMTU7MjAxNS0yMDE2OzIwMTYtMjAxNzsyMDE3LTIwMTg7MjAxOC0yMDE5Oz47QDxcZTsyMDAxLTIwMDI7MjAwMi0yMDAzOzIwMDMtMjAwNDsyMDA0LTIwMDU7MjAwNS0yMDA2OzIwMDYtMjAwNzsyMDA3LTIwMDg7MjAwOC0yMDA5OzIwMDktMjAxMDsyMDEwLTIwMTE7MjAxMS0yMDEyOzIwMTItMjAxMzsyMDEzLTIwMTQ7MjAxNC0yMDE1OzIwMTUtMjAxNjsyMDE2LTIwMTc7MjAxNy0yMDE4OzIwMTgtMjAxOTs+Pjs+Ozs+O3Q8cDw7cDxsPG9uY2xpY2s7PjtsPHdpbmRvdy5wcmludCgpXDs7Pj4+Ozs+O3Q8cDw7cDxsPG9uY2xpY2s7PjtsPHdpbmRvdy5jbG9zZSgpXDs7Pj4+Ozs+O3Q8cDxwPGw8VmlzaWJsZTs+O2w8bzx0Pjs+Pjs+Ozs+O3Q8O2w8aTwwPjtpPDE+O2k8Mz47aTw1PjtpPDc+O2k8OT47aTwxMT47aTwyMT47PjtsPHQ8QDA8Ozs7Ozs7Ozs7Oz47Oz47dDxAMDw7Ozs7Ozs7Ozs7Pjs7Pjt0PEAwPDs7Ozs7Ozs7Ozs+Ozs+O3Q8QDA8Ozs7Ozs7Ozs7Oz47Oz47dDxAMDw7Ozs7Ozs7Ozs7Pjs7Pjt0PEAwPHA8cDxsPFZpc2libGU7PjtsPG88Zj47Pj47Pjs7Ozs7Ozs7Ozs+Ozs+O3Q8QDA8cDxwPGw8VmlzaWJsZTs+O2w8bzxmPjs+Pjs+Ozs7Ozs7Ozs7Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDx6Zjs+Pjs+Ozs+Oz4+O3Q8QDA8Ozs7Ozs7Ozs7Oz47Oz47dDxAMDw7Ozs7Ozs7Ozs7Pjs7Pjs+Pjs+Pjs+fBoBDHmTywIM0r+EQXO8f8JORF4=");
		// 这个参数是学年
		NameValuePair e7 = new BasicNameValuePair("ddlXN", year);
		// 这个参数是学期，这里有个小技巧（传0的话，不管学年传多少，返回的视乎都是最新的成绩，而且响应时间特别快）
		NameValuePair e8 = new BasicNameValuePair("ddlXQ", semeste);
		// 这个参数是页面上的按学期查询按钮 表示按学期查询
		NameValuePair e9 = new BasicNameValuePair("Button1", "按学期查询");

		pairs.add(e6);
		pairs.add(e7);
		pairs.add(e8);
		pairs.add(e9);
		// 将参数进行URL编码，此处留个疑问，为什么URL编码要使用GB2312字符集编码
		UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(pairs, Charset.forName("GB2312"));
		// 将参数放到httpPost中去
		httpPost.setEntity(urlEncodedFormEntity);

		try {
			// 发起请求
			HttpResponse httpResponse = httpClient.execute(httpPost);

			// 这是响应实体
			HttpEntity responseEntity = httpResponse.getEntity();

			// 将响应实体转成字符串
			String html = EntityUtils.toString(responseEntity);

			// 回收responseEntity
			EntityUtils.consume(responseEntity);
			// 回收urlEncodedFormEntity
			EntityUtils.consume(urlEncodedFormEntity);

			return ScoreStringPolisher.polish(html);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		// return "获取成绩时发生异常，请重试或联系管理员。";
	}
}
