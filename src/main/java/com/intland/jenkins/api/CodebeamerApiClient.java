/*
 * Copyright (c) 2016 Intland Software (support@intland.com)
 */
package com.intland.jenkins.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intland.jenkins.api.dto.*;
import com.intland.jenkins.coverage.ExecutionContext;
import jcifs.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A service class that implement the data transfer between the codeBeamer and
 * the jenkins plugin
 *
 * @author abanfi
 */
public class CodebeamerApiClient {

	private static Set<Integer> SUCCESSFUL_STATUSES = new HashSet<>(
			Arrays.asList(HttpStatus.SC_ACCEPTED, HttpStatus.SC_OK, HttpStatus.SC_CREATED,
					HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION, HttpStatus.SC_NO_CONTENT, HttpStatus.SC_RESET_CONTENT,
					HttpStatus.SC_PARTIAL_CONTENT, HttpStatus.SC_MULTI_STATUS));

	private final static int HTTP_TIMEOUT = 300000;
	private HttpClient client;
	private RequestConfig requestConfig;
	private String baseUrl;
	private ObjectMapper objectMapper;

	public CodebeamerApiClient(String uri, String username, String password) {
		this.baseUrl = uri;

		this.objectMapper = new ObjectMapper();

		// initialize rest client
        // http://stackoverflow.com/questions/9539141/httpclient-sends-out-two-requests-when-using-basic-auth
		final String authHeader = "Basic " + Base64.encode((username + ":" + password).getBytes(StandardCharsets.UTF_8));

		HashSet<Header> defaultHeaders = new HashSet<Header>();
		defaultHeaders.add(new BasicHeader(HttpHeaders.AUTHORIZATION, authHeader));

		this.client = HttpClientBuilder
				.create()
				.setDefaultHeaders(defaultHeaders)
				.build();
		this.requestConfig = RequestConfig
				.custom()
				.setConnectionRequestTimeout(HTTP_TIMEOUT)
				.setConnectTimeout(HTTP_TIMEOUT)
				.setSocketTimeout(HTTP_TIMEOUT)
				.build();
	}

	/**
	 * Finds the tracker item by name in the specified tracker. If it is not
	 * exists the it create with the specified parameters
	 *
	 * @param trackerId
	 *            the item's tracker id
	 * @param name
	 *            the tracker's name
	 * @param description
	 *            the new item's description
	 * @return the found or the newly created tracker item
	 */
	public TrackerItemDto findOrCreateTestSet(ExecutionContext context, Integer trackerId, String name,
			String description) throws IOException {

		String urlParamName = this.encodeParam(name);
		String requestUrl = String.format(this.baseUrl + "/rest/tracker/%s/items/or/name=%s/page/1", trackerId,
				urlParamName);
		context.logFormat("Call URL <%s> for tracker item.", requestUrl);

		PagedTrackerItemsDto pagedTrackerItemsDto = this.get(requestUrl, PagedTrackerItemsDto.class);

		if (pagedTrackerItemsDto.getTotal() > 0) {
			TrackerItemDto testSetDto = pagedTrackerItemsDto.getItems().get(0);
			context.logFormat("%d Tracker item found, returns with the first: <%s>", pagedTrackerItemsDto.getTotal(),
					testSetDto);
			return testSetDto;
		} else {

			context.log("There is no master test set yet.");
			TestRunDto testSetDto = new TestRunDto();
			testSetDto.setName(name);
			testSetDto.setTracker("/tracker/" + trackerId);
			testSetDto.setDescription(description);

			TrackerItemDto trackerItem = this.postTrackerItem(context, testSetDto);
			context.logFormat("New test set succesfully created: <%s>", trackerItem);
			return trackerItem;
		}
	}

	/**
	 * Encode URL parameters
	 *
	 * @param param
	 *            the parameter to encode
	 *
	 * @return the encoded parameter
	 */
	public String encodeParam(String param) {
		try {
			String result = URLEncoder.encode(param, "UTF-8");
			return result.replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			return param;
		}
	}

	/**
	 * Post the specified tracker item to the codeBeamer
	 *
	 * @param context
	 * @param testRunDto
	 * @return
	 */
	public TrackerItemDto postTrackerItem(ExecutionContext context, TestRunDto testRunDto) throws IOException {
		String content = this.objectMapper.writeValueAsString(testRunDto);
		return this.post(context, content);
	}

	/**
	 * Get all tracker item from the specified tracker
	 *
	 * @param trackerId
	 *            the tracker's id
	 * @return all of the tracker item in the tracker
	 */
	public List<TrackerItemDto> getTestCaseList(ExecutionContext context) throws IOException {

		String url = "%s/rest/tracker/%s/items/page/%s?pagesize=500";

		List<TrackerItemDto> items = new ArrayList<>();
		int total = 0;
		int pageCount = 1;
		do {
			String pageUrl = String.format(url, this.baseUrl, context.getTestCaseTrackerId(), pageCount);
			context.logFormat("Loading Test Case Page: <%s>", pageUrl);

			// get a page from codebeamer
			PagedTrackerItemsDto pagedTrackerItemsDto = this.get(pageUrl, PagedTrackerItemsDto.class);
			total = pagedTrackerItemsDto.getTotal();
			items.addAll(pagedTrackerItemsDto.getItems());

			context.logFormat("Page %d loaded, all items: %d (all items count: %d)", pageCount, items.size(), total);
			pageCount++;
		} while (items.size() < total);

		return items;
	}

	/**
	 * Checks specified test case tracker is support the given test case type or
	 * not
	 *
	 * @param testCaseTrackerId
	 * @param testCaseType
	 * @return
	 */
	public boolean isTestCaseTypeSupported(Integer testCaseTrackerId, String testCaseType) throws IOException {
		String url = String.format("%s/rest/tracker/%s/schema", this.baseUrl, testCaseTrackerId);
		TrackerSchemaDto trackerSchemaDto = this.get(url, TrackerSchemaDto.class);
		return trackerSchemaDto.doesTypeContain(testCaseType);
	}

	/**
	 * Updates the specified test case status
	 *
	 * @param context
	 *            the execution context
	 * @param id
	 *            the test case id to update
	 * @param status
	 *            the new status
	 * @return the updated value
	 */
	public TrackerItemDto updateTestCaseStatus(ExecutionContext context, Integer id, String status) throws IOException {
		TestCaseDto testCaseDto = new TestCaseDto(id, status);
		String content = this.objectMapper.writeValueAsString(testCaseDto);
		return this.put(context, content);
	}

	/**
	 * Fetch a tracker item from the codeBeamer by the specified id
	 *
	 * @param itemId
	 * @return
	 */
	public TrackerItemDto getTrackerItem(Integer itemId) throws IOException {
		return this.get(String.format("%s/rest/item/%s", this.baseUrl, itemId), TrackerItemDto.class);
	}

	/**
	 * Fetch the tracker for the specified tracker id
	 *
	 * @param trackerId
	 * @return
	 */
	public TrackerDto getTracker(Integer trackerId) throws IOException {
		return this.get(String.format("%s/rest/tracker/%s", this.baseUrl, trackerId), TrackerDto.class);
	}

	/**
	 * Posts a tracker item
	 *
	 * @param context
	 * @param content
	 * @return
	 */
	private TrackerItemDto post(ExecutionContext context, String content) {

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		HttpPost post = new HttpPost(String.format("%s/rest/item", this.baseUrl));
		post.setConfig(this.requestConfig);

		StringEntity stringEntity = new StringEntity(content, "UTF-8");
		stringEntity.setContentType("application/json");
		post.setEntity(stringEntity);

		context.logFormat("Execute post request /rest/item with content: <%s>", StringUtils.abbreviate(content, 200));

		try {
			HttpResponse response = this.client.execute(post);

			Integer statusCode = response.getStatusLine().getStatusCode();
			if (SUCCESSFUL_STATUSES.contains(statusCode)) {
				String json = new BasicResponseHandler().handleResponse(response);

				TrackerItemDto readValue = this.objectMapper.readValue(json, TrackerItemDto.class);
				stopWatch.stop();
				context.logFormat("Post request completed in: %d ms", stopWatch.getTime());
				return readValue;
			}

			context.logFormat("Failed to post tracker item! Return code: %d", statusCode);

			return null;
		} catch (Exception e) {
			context.logFormat("Failed to post tracker item! Exception message: %s", e.getMessage());
			throw new RuntimeException(e);
		} finally {
			post.releaseConnection();
		}

	}

	/**
	 * Put (update) an existing item to the codeBeamer
	 *
	 * @param context
	 * @param dto
	 * @return
	 */
	public TrackerItemDto put(ExecutionContext context, Object dto) throws IOException {
		String content = this.objectMapper.writeValueAsString(dto);
		return this.put(context, content);
	}

	/**
	 * Put the specified content as a tracker item json to codeBeamer
	 *
	 * @param context
	 * @param content
	 * @return
	 */
	private TrackerItemDto put(ExecutionContext context, String content) throws IOException {

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		context.logFormat("Execute put request /rest/item with content: <%s>", StringUtils.abbreviate(content, 200));

		HttpPut put = new HttpPut(String.format("%s/rest/item", this.baseUrl));
		put.setConfig(this.requestConfig);

		StringEntity stringEntity = new StringEntity(content, "UTF-8");
		stringEntity.setContentType("application/json");
		put.setEntity(stringEntity);

		try {
			HttpResponse response = this.client.execute(put);

			Integer statusCode = response.getStatusLine().getStatusCode();
			if (SUCCESSFUL_STATUSES.contains(statusCode)) {
				String json = new BasicResponseHandler().handleResponse(response);

				TrackerItemDto readValue = this.objectMapper.readValue(json, TrackerItemDto.class);
				stopWatch.stop();
				context.logFormat("Put request completed in: %d ms", stopWatch.getTime());
				return readValue;
			}

			context.logFormat("Failed to put tracker item! Return code: %d", statusCode);

			return null;
		} catch (Exception e) {
			context.logFormat("Failed to put tracker item! Exception message: %s", e.getMessage());
			throw new RuntimeException(e);
		} finally {
			put.releaseConnection();
		}
	}

	/**
	 * Executes a get HTTP request to the codeBeamer with the specified URL and
	 * tries to parse the result as the given result class
	 *
	 * @param url
	 * @param resultClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T> T get(String url, Class<?> resultClass) {

		HttpGet get = new HttpGet(url);
		get.setConfig(this.requestConfig);

		try {
			HttpResponse response = this.client.execute(get);
			Integer statusCode = response.getStatusLine().getStatusCode();
			String result = null;
			if (SUCCESSFUL_STATUSES.contains(statusCode)) {
				result = new BasicResponseHandler().handleResponse(response);
				return (T) this.objectMapper.readValue(result, resultClass);
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			get.releaseConnection();
		}
	}

}
