package com.israel_martinez.hackernewsalgolia.EDA;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HighlightResult {

@SerializedName("author")
@Expose
private Author author;
@SerializedName("comment_text")
@Expose
private CommentText commentText;
@SerializedName("story_title")
@Expose
private StoryTitle storyTitle;
@SerializedName("story_url")
@Expose
private StoryUrl storyUrl;
@SerializedName("title")
@Expose
private Title title;
@SerializedName("story_text")
@Expose
private StoryText storyText;
@SerializedName("url")
@Expose
private Url url;

public Author getAuthor() {
return author;
}

public void setAuthor(Author author) {
this.author = author;
}

public CommentText getCommentText() {
return commentText;
}

public void setCommentText(CommentText commentText) {
this.commentText = commentText;
}

public StoryTitle getStoryTitle() {
return storyTitle;
}

public void setStoryTitle(StoryTitle storyTitle) {
this.storyTitle = storyTitle;
}

public StoryUrl getStoryUrl() {
return storyUrl;
}

public void setStoryUrl(StoryUrl storyUrl) {
this.storyUrl = storyUrl;
}

public Title getTitle() {
return title;
}

public void setTitle(Title title) {
this.title = title;
}

public StoryText getStoryText() {
return storyText;
}

public void setStoryText(StoryText storyText) {
this.storyText = storyText;
}

public Url getUrl() {
return url;
}

public void setUrl(Url url) {
this.url = url;
}

}