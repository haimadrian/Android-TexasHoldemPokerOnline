package org.hit.android.haim.texasholdem.web;

import com.google.gson.reflect.TypeToken;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for performing Story requests
 * <ul>
 *     <li>{@link #upload(Story) Upload}</li>
 *     <li>{@link #update(Story) Update}</li>
 *     <li>{@link #getStory(Long) Get story}</li>
 *     <li>{@link #getStoriesByHeroName(String) Get stories by hero name}</li>
 *     <li>{@link #getStoriesByTitle(String) Get stories by title}</li>
 *     <li>{@link #getStoriesByLocation(String) Get stories by location}</li>
 *     <li>{@link #getStoriesByUserId(String) Get stories by user identifier}</li>
 *     <li>{@link #getStoriesByCoordinateId(Long) Get stories by coordinate identifier}</li>
 * </ul>
 * @author Haim Adrian
 * @since 23-Mar-2020
 */
public class StoryService {
    // Hide ctor. Access this class through MapStoriesService.
    StoryService() {

    }

}
