/*
 * Copyright (c) 2016 Intland Software (support@intland.com)
 */
package com.intland.jenkins.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intland.jenkins.api.dto.PagedTrackerItemsDto;
import com.intland.jenkins.api.dto.TestCaseDto;
import com.intland.jenkins.api.dto.TestRunDto;
import com.intland.jenkins.api.dto.TrackerDto;
import com.intland.jenkins.api.dto.TrackerItemDto;
import com.intland.jenkins.api.dto.TrackerSchemaDto;
import com.intland.jenkins.api.dto.UserDto;
import com.intland.jenkins.coverage.ExecutionContext;

public class CodebeamerApiClient {

	private final int HTTP_TIMEOUT = 300000;
	private HttpClient client;
	private RequestConfig requestConfig;
	private String baseUrl;
	private ObjectMapper objectMapper;

	public CodebeamerApiClient(String uri, String username, String password) {
		this.baseUrl = uri;

		this.objectMapper = new ObjectMapper();
		CredentialsProvider provider = this.getCredentialsProvider(username, password);
		this.client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
		this.requestConfig = RequestConfig.custom().setConnectionRequestTimeout(this.HTTP_TIMEOUT)
				.setConnectTimeout(this.HTTP_TIMEOUT).setSocketTimeout(this.HTTP_TIMEOUT).build();
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
	 * @throws IOException
	 */
	public TrackerItemDto findOrCreateTrackerItem(ExecutionContext context, Integer trackerId, String name,
			String description) throws IOException {
		String urlParamName = this.encodeParam(name);
		String content = this
				.get(String.format(this.baseUrl + "/rest/tracker/%s/items/or/name=%s/page/1", trackerId, urlParamName));
		PagedTrackerItemsDto pagedTrackerItemsDto = this.objectMapper.readValue(content, PagedTrackerItemsDto.class);

		if (pagedTrackerItemsDto.getTotal() > 0) {
			return pagedTrackerItemsDto.getItems()[0];
		} else {
			TestRunDto testConfig = new TestRunDto();
			testConfig.setName(name);
			testConfig.setTracker("/tracker/" + trackerId);
			testConfig.setDescription(description);
			return this.postTrackerItem(context, testConfig);
		}
	}

	public String encodeParam(String param) {
		try {
			String result = URLEncoder.encode(param, "UTF-8");
			return result.replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			return param;
		}
	}

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
	 * @throws IOException
	 */
	public List<TrackerItemDto> getTrackerItemList(ExecutionContext context) throws IOException {

		String url = "%s/rest/tracker/%s/items/page/%s?pagesize=500";

		List<TrackerItemDto> items = new ArrayList<>();
		int total = 0;
		int page = 1;
		do {
			context.logFormat("Loading Test case page %d", page);
			// get a page from codebeamer
			String json = this.get(String.format(url, this.baseUrl, context.getTestCaseTrackerId(), page));
			PagedTrackerItemsDto pagedTrackerItemsDto = this.objectMapper.readValue(json, PagedTrackerItemsDto.class);
			total = pagedTrackerItemsDto.getTotal();
			items.addAll(Arrays.asList(pagedTrackerItemsDto.getItems()));

			context.logFormat("Page %d loaded, all items: %d (all items count: %d)", page, items.size(), total);
			page++;
		} while (items.size() < total);

		return items;
	}

	@Deprecated
	public TrackerItemDto[] getTrackerItems(Integer trackerId) throws IOException {
		String url = String.format("%s/rest/tracker/%s/items/page/1?pagesize=500", this.baseUrl, trackerId);
		String json = this.get(url);
		PagedTrackerItemsDto pagedTrackerItemsDto = this.objectMapper.readValue(json, PagedTrackerItemsDto.class);

		int numberOfRequests = (pagedTrackerItemsDto.getTotal() / 500) + 1;
		List<TrackerItemDto> items = Arrays.asList(pagedTrackerItemsDto.getItems());
		for (int i = 2; i < numberOfRequests; i++) {
			url = String.format("%s/rest/tracker/%s/items/page/%s?pagesize=500", this.baseUrl, trackerId,
					numberOfRequests);
			json = this.get(url);
			pagedTrackerItemsDto = this.objectMapper.readValue(json, PagedTrackerItemsDto.class);
			items.addAll(Arrays.asList(pagedTrackerItemsDto.getItems()));
		}

		return items.toArray(new TrackerItemDto[items.size()]);
	}

	public boolean isTestCaseTypeSupported(Integer testCaseTrackerId, String testCaseType) throws IOException {
		String url = String.format("%s/rest/tracker/%s/schema", this.baseUrl, testCaseTrackerId);
		String json = this.get(url);
		TrackerSchemaDto trackerSchemaDto = this.objectMapper.readValue(json, TrackerSchemaDto.class);
		return trackerSchemaDto.doesTypeContain(testCaseType);
	}

	public TrackerItemDto updateTrackerItemStatus(ExecutionContext context, Integer id, String status)
			throws IOException {
		TestCaseDto testCaseDto = new TestCaseDto(id, status);
		String content = this.objectMapper.writeValueAsString(testCaseDto);
		return this.put(context, content);
	}

	public TrackerItemDto getTrackerItem(Integer itemId) throws IOException {
		String value = this.get(this.baseUrl + "/rest/item/" + itemId);
		return value != null ? this.objectMapper.readValue(value, TrackerItemDto.class) : null;
	}

	public TrackerDto getTrackerType(Integer trackerId) throws IOException {
		String value = this.get(this.baseUrl + "/rest/tracker/" + trackerId);
		return value != null ? this.objectMapper.readValue(value, TrackerDto.class) : null;
	}

	public String getUserId(String author) throws IOException {
		String authorNoSpace = author.replaceAll(" ", "");
		String tmpUrl = String.format("%s/rest/user/%s", this.baseUrl, authorNoSpace);

		String httpResult = this.get(tmpUrl);
		String result = null;

		if (httpResult != null) { // 20X success
			UserDto userDto = this.objectMapper.readValue(httpResult, UserDto.class);
			String uri = userDto.getUri();
			result = uri.substring(uri.lastIndexOf("/") + 1);
		}

		return result;
	}

	private TrackerItemDto post(ExecutionContext context, String content) throws IOException {

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		HttpPost post = new HttpPost(String.format("%s/rest/item", this.baseUrl));
		post.setConfig(this.requestConfig);

		StringEntity stringEntity = new StringEntity(content, "UTF-8");
		stringEntity.setContentType("application/json");
		post.setEntity(stringEntity);

		try {
			HttpResponse response = this.client.execute(post);
			String json = new BasicResponseHandler().handleResponse(response);
			post.releaseConnection();

			stopWatch.stop();

			context.log("Post request completed in: " + stopWatch.getTime());

			stopWatch = new StopWatch();
			stopWatch.start();

			TrackerItemDto readValue = this.objectMapper.readValue(json, TrackerItemDto.class);
			stopWatch.stop();
			context.log("Post result parsed in: " + stopWatch.getTime());
			return readValue;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

	public TrackerItemDto put(ExecutionContext context, Object dto) throws IOException {
		String content = this.objectMapper.writeValueAsString(dto);
		return this.put(context, content);
	}

	private TrackerItemDto put(ExecutionContext context, String content) throws IOException {

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		HttpPut put = new HttpPut(String.format("%s/rest/item", this.baseUrl));
		put.setConfig(this.requestConfig);

		StringEntity stringEntity = new StringEntity(content, "UTF-8");
		stringEntity.setContentType("application/json");
		put.setEntity(stringEntity);

		HttpResponse response = this.client.execute(put);
		String json = new BasicResponseHandler().handleResponse(response);
		put.releaseConnection();

		stopWatch.stop();
		context.log("Put request completed in: " + stopWatch.getTime());
		stopWatch = new StopWatch();
		stopWatch.start();

		TrackerItemDto readValue = this.objectMapper.readValue(json, TrackerItemDto.class);
		stopWatch.stop();
		context.log("Post result parsed in: " + stopWatch.getTime());

		return readValue;
	}

	private String get(String url) throws IOException {
		HttpGet get = new HttpGet(url);
		get.setConfig(this.requestConfig);
		HttpResponse response = this.client.execute(get);
		int statusCode = response.getStatusLine().getStatusCode();

		String result = null;
		if (statusCode == 200) {
			result = new BasicResponseHandler().handleResponse(response);
		}

		get.releaseConnection();

		return result;
	}

	private CredentialsProvider getCredentialsProvider(String username, String password) {
		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
		provider.setCredentials(AuthScope.ANY, credentials);
		return provider;
	}
}
