package com.cleverpush.banner.models.blocks;

import com.cleverpush.banner.models.BannerAction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class BannerButtonBlock extends BannerBlock {
  private String text;
  private String color;
  private String darkColor;
  private String background;
  private String darkBackground;
  private int size;
  private Alignment alignment;
  private boolean dismiss;
  private int radius;
  private BannerAction action;
  private List<BannerBlockScreen> blockScreens;

  private BannerButtonBlock() {
  }

  public String getText() {
    return text;
  }

  public String getColor() {
    return color;
  }

  public String getDarkColor() {
    return darkColor;
  }

  public String getBackground() {
    return background;
  }

  public String getDarkBackground() {
    return darkBackground;
  }

  public int getSize() {
    return size;
  }

  public Alignment getAlignment() {
    return alignment;
  }

  public int getRadius() {
    return radius;
  }

  public BannerAction getAction() {
    return action;
  }

  public List<BannerBlockScreen> getBlocks() {
    return blockScreens;
  }


  public static BannerButtonBlock createButtonBlock(JSONObject json) throws JSONException {
    BannerButtonBlock buttonBlock = new BannerButtonBlock();

    buttonBlock.type = BannerBlockType.Button;

    buttonBlock.text = json.getString("text");

    buttonBlock.color = json.getString("color");
    if (json.has("darkColor") && !json.optString("darkColor").isEmpty()) {
      buttonBlock.darkColor = json.optString("darkColor");
    }

    buttonBlock.background = json.getString("background");
    if (json.has("darkBackground") && !json.optString("darkBackground").isEmpty()) {
      buttonBlock.darkBackground = json.optString("darkBackground");
    }

    buttonBlock.size = json.getInt("size");
    buttonBlock.alignment = Alignment.fromString(json.getString("alignment"));
    buttonBlock.radius = json.getInt("radius");
    buttonBlock.action = BannerAction.create(json.getJSONObject("action"));
    buttonBlock.blockScreens = new LinkedList<>();

    if (json.has("screens")) {
      JSONArray blockArray = json.getJSONArray("screens");
      for (int i = 0; i < blockArray.length(); ++i) {
        buttonBlock.blockScreens.add(BannerBlockScreen.create(blockArray.getJSONObject(i)));
      }
    }

    return buttonBlock;
  }
}
