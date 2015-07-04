package de.corux.scm.plugins.fisheye;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for mappings of scm repository to fisheye repositories.
 */
public class FisheyeReposDto
{
    public String repository;
    public List<String> currentFisheyeRepositories = new ArrayList<String>();
    public List<String> newFisheyeRepositories = new ArrayList<String>();
}