package com.cleverpush.stories;

import static com.cleverpush.stories.StoryView.DEFAULT_BACKGROUND_COLOR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.cleverpush.ActivityLifecycleListener;
import com.cleverpush.CleverPush;
import com.cleverpush.util.FontUtils;
import com.cleverpush.util.Logger;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cleverpush.R;
import com.cleverpush.stories.listener.OnItemClickListener;
import com.cleverpush.stories.models.Story;

import java.util.ArrayList;

public class StoryViewListAdapter extends RecyclerView.Adapter<StoryViewHolder> {

  private int DEFAULT_BORDER_COLOR = Color.BLACK;
  private int DEFAULT_TEXT_COLOR = Color.BLACK;
  private int DEFAULT_UNREAD_COUNT_BACKGROUND_COLOR = Color.BLACK;
  private int DEFAULT_UNREAD_COUNT_TEXT_COLOR = Color.WHITE;

  private Context context;
  private ArrayList<Story> stories;
  private OnItemClickListener onItemClickListener;
  private TypedArray typedArray;
  public static StoryViewListAdapter storyViewListAdapter;
  private int parentLayoutWidth;
  private static final String TAG = "CleverPush/StoryViewAdapter";

  public StoryViewListAdapter(Context context, ArrayList<Story> stories, TypedArray typedArray,
                              OnItemClickListener onItemClickListener, int parentLayoutWidth) {
    if (context == null) {
      if (CleverPush.getInstance(CleverPush.context).getCurrentContext() != null) {
        this.context = CleverPush.getInstance(CleverPush.context).getCurrentContext();
      }
    } else {
      this.context = context;
    }
    this.stories = stories;
    this.typedArray = typedArray;
    this.onItemClickListener = onItemClickListener;
    this.parentLayoutWidth = parentLayoutWidth;
  }

  @Override
  public StoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    try {
      if (context == null) {
        Logger.e(TAG, "Context is null");
        return null;
      }
      LayoutInflater inflater = LayoutInflater.from(context);
      View itemViewStoryHead = inflater.inflate(R.layout.item_view_story, parent, false);
      return new StoryViewHolder(itemViewStoryHead);
    } catch (Exception e) {
      Logger.e(TAG, "Error in onCreateViewHolder of StoryViewListAdapter", e);
      return null;
    }
  }

  @SuppressLint("ResourceType")
  @Override
  public void onBindViewHolder(StoryViewHolder holder, int position) {
    try {
      TextView nameTextView = (TextView) holder.itemView.findViewById(R.id.tvTitle);
      TextView unreadCountTextView = (TextView) holder.itemView.findViewById(R.id.tvUnreadCount);
      FrameLayout unreadCountFrameLayout = (FrameLayout) holder.itemView.findViewById(R.id.unreadCountFrameLayout);
      RelativeLayout unreadCountRelativeLayout = (RelativeLayout) holder.itemView.findViewById(R.id.unreadCountRelativeLayout);
      ImageView image = (ImageView) holder.itemView.findViewById(R.id.ivChallenge);
      CardView cardView = (CardView) holder.itemView.findViewById(R.id.ivChallengeCardView);
      CardView cardViewShadow = (CardView) holder.itemView.findViewById(R.id.cardViewShadow);
      LinearLayout borderLayout = (LinearLayout) holder.itemView.findViewById(R.id.borderLayout);
      LinearLayout storyLayout = (LinearLayout) holder.itemView.findViewById(R.id.storyLayout);
      LinearLayout imageLayout = (LinearLayout) holder.itemView.findViewById(R.id.imageLayout);
      LinearLayout parentLayout = (LinearLayout) holder.itemView.findViewById(R.id.parentLayout);
      RelativeLayout titleInsideLayout = (RelativeLayout) holder.itemView.findViewById(R.id.titleInsideLayout);
      TextView tvTitleInside = (TextView) holder.itemView.findViewById(R.id.tvTitleInside);

      int iconHeight = (int) typedArray.getDimension(R.styleable.StoryView_story_icon_height, 206);
      int iconWidth = (int) typedArray.getDimension(R.styleable.StoryView_story_icon_width, 206);
      boolean iconShadow = typedArray.getBoolean(R.styleable.StoryView_story_icon_shadow, false);
      int borderVisibility = typedArray.getInt(R.styleable.StoryView_border_visibility, View.VISIBLE);
      float borderMargin = typedArray.getDimension(R.styleable.StoryView_border_margin, 5.0F);
      int borderWidth = (int) typedArray.getDimension(R.styleable.StoryView_border_width, 5);
      float cornerRadius = typedArray.getDimension(R.styleable.StoryView_story_icon_corner_radius, -1);
      int subStoryUnreadCount = typedArray.getInt(R.styleable.StoryView_sub_story_unread_count_visibility, View.GONE);
      boolean restrictToThreeItems = typedArray.getBoolean(R.styleable.StoryView_restrict_to_three_items, false);
      float iconSpace = typedArray.getDimension(R.styleable.StoryView_story_icon_space, -1);
      int titlePosition = typedArray.getInt(R.styleable.StoryView_title_position, 0);
      int titleVisibility = typedArray.getInt(R.styleable.StoryView_title_visibility, View.VISIBLE);
      int storyViewBackgroundColor = typedArray.getColor(R.styleable.StoryView_background_color, DEFAULT_BACKGROUND_COLOR);

      parentLayout.setBackgroundColor(storyViewBackgroundColor);

      int padding = convertDpToPx(context, 3);

      parentLayout.setPadding(padding, padding, padding, padding);

      if (restrictToThreeItems) {
        int width = parentLayoutWidth / 3;
        if (iconSpace != -1) {
          width = (int) (width - iconSpace);
        }
        if (subStoryUnreadCount == 0) {
          width = width - 30;
        }
        width = width - (padding * 2);
        iconWidth = width;
      }

      if (subStoryUnreadCount == 0) {
        unreadCountTextView.setVisibility(View.VISIBLE);
        if (stories.get(position).isOpened() && stories.get(position).getUnreadCount() == 0) {
          unreadCountTextView.setVisibility(View.GONE);
        } else {
          unreadCountTextView.setText(stories.get(position).getUnreadCount() + "");
        }
        unreadCountTextView.setTextColor(typedArray.getColor(R.styleable.StoryView_sub_story_unread_count_text_color, DEFAULT_UNREAD_COUNT_TEXT_COLOR));

        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);
        int backgroundColor = typedArray.getColor(R.styleable.StoryView_sub_story_unread_count_background_color, DEFAULT_UNREAD_COUNT_BACKGROUND_COLOR);
        circleDrawable.setColor(backgroundColor);

        unreadCountTextView.setBackground(circleDrawable);

        ViewGroup.LayoutParams unreadCountFrameLayoutParams = unreadCountFrameLayout.getLayoutParams();
        unreadCountFrameLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        if (cornerRadius == -1) {
          unreadCountFrameLayoutParams.width = iconWidth;
        } else {
          unreadCountFrameLayoutParams.width = iconWidth + 30;
        }
        unreadCountFrameLayout.setLayoutParams(unreadCountFrameLayoutParams);

        ViewGroup.LayoutParams unreadCountRelativeLayoutParams = unreadCountRelativeLayout.getLayoutParams();
        unreadCountRelativeLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        if (cornerRadius == -1) {
          unreadCountRelativeLayoutParams.width = iconWidth;
        } else {
          unreadCountRelativeLayoutParams.width = iconWidth + 30;
        }
        unreadCountRelativeLayout.setLayoutParams(unreadCountRelativeLayoutParams);

      } else {
        unreadCountTextView.setVisibility(View.GONE);

        ViewGroup.LayoutParams unreadCountFrameLayoutParams = unreadCountFrameLayout.getLayoutParams();
        unreadCountFrameLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;;
        unreadCountFrameLayoutParams.width = iconWidth;
        unreadCountFrameLayout.setLayoutParams(unreadCountFrameLayoutParams);

        ViewGroup.LayoutParams unreadCountRelativeLayoutParams = unreadCountRelativeLayout.getLayoutParams();
        unreadCountRelativeLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;;
        unreadCountRelativeLayoutParams.width = iconWidth;
        unreadCountRelativeLayout.setLayoutParams(unreadCountRelativeLayoutParams);
      }

      if (borderVisibility == 0 && !stories.get(position).isOpened()) {
        ViewGroup.LayoutParams imageParams = image.getLayoutParams();
        if (cornerRadius == -1) {
          imageParams.height = (int) (iconHeight - borderMargin - 12);
        } else {
          imageParams.height = iconHeight;
        }
        imageParams.width = (int) (iconWidth - borderMargin - 12);
        image.setLayoutParams(imageParams);

        ViewGroup.LayoutParams titleInsideLayoutParams = titleInsideLayout.getLayoutParams();
        if (cornerRadius == -1) {
          titleInsideLayoutParams.height = (int) (iconHeight - borderMargin - 12);
        } else {
          titleInsideLayoutParams.height = iconHeight;
        }
        titleInsideLayoutParams.width = (int) (iconWidth - borderMargin - 12);
        titleInsideLayout.setLayoutParams(titleInsideLayoutParams);

        ViewGroup.LayoutParams cardParams = cardView.getLayoutParams();
        if (cornerRadius == -1) {
          cardParams.height = (int) (iconHeight - borderMargin - 12);
        } else {
          cardParams.height = iconHeight;
        }
        cardParams.width = (int) (iconWidth - borderMargin - 12);
        cardView.setLayoutParams(cardParams);

        ViewGroup.LayoutParams imageLayoutParams = imageLayout.getLayoutParams();
        imageLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        imageLayoutParams.width = (int) (iconWidth - borderMargin - 12);
        imageLayout.setLayoutParams(imageLayoutParams);

        ViewGroup.LayoutParams cardViewShadowParams = cardViewShadow.getLayoutParams();
        if (iconShadow) {
          cardViewShadowParams.height = iconHeight + 7;
        } else {
          if (cornerRadius == -1) {
            cardViewShadowParams.height = (int) (iconHeight - borderMargin - 12);
          } else {
            cardViewShadowParams.height = iconHeight;
          }
        }
        cardViewShadowParams.width = (int) (iconWidth - borderMargin - 12);
        cardViewShadow.setLayoutParams(cardViewShadowParams);
      } else {
        ViewGroup.LayoutParams imageParams = image.getLayoutParams();
        imageParams.height = iconHeight;
        imageParams.width = iconWidth;
        image.setLayoutParams(imageParams);

        ViewGroup.LayoutParams titleInsideLayoutParams = titleInsideLayout.getLayoutParams();
        titleInsideLayoutParams.height = iconHeight;
        titleInsideLayoutParams.width = iconWidth;
        titleInsideLayout.setLayoutParams(titleInsideLayoutParams);

        ViewGroup.LayoutParams cardParams = cardView.getLayoutParams();
        cardParams.height = iconHeight;
        cardParams.width = iconWidth;
        cardView.setLayoutParams(cardParams);

        ViewGroup.LayoutParams imageLayoutParams = imageLayout.getLayoutParams();
        imageLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        imageLayoutParams.width = iconWidth;
        imageLayout.setLayoutParams(imageLayoutParams);

        ViewGroup.LayoutParams cardViewShadowParams = cardViewShadow.getLayoutParams();
        if (iconShadow) {
          cardViewShadowParams.height = iconHeight + 7;
        } else {
          cardViewShadowParams.height = iconHeight;
        }
        cardViewShadowParams.width = iconWidth;
        cardViewShadow.setLayoutParams(cardViewShadowParams);
      }

      ViewGroup.LayoutParams storyLayoutParams = storyLayout.getLayoutParams();
      storyLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
      storyLayoutParams.width = iconWidth;
      storyLayout.setLayoutParams(storyLayoutParams);

      if (titleVisibility == 0) {
        if (titlePosition == 0) {
          nameTextView.setVisibility(View.VISIBLE);
          titleInsideLayout.setVisibility(View.GONE);

          ViewGroup.LayoutParams nameTextViewParams = nameTextView.getLayoutParams();
          nameTextViewParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
          nameTextViewParams.width = iconWidth;
          nameTextView.setLayoutParams(nameTextViewParams);

          int titleTextSize = typedArray.getDimensionPixelSize(R.styleable.StoryView_title_text_size, 32);
          nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
          nameTextView.setText(stories.get(position).getTitle());
          nameTextView.setTextColor(typedArray.getColor(R.styleable.StoryView_text_color, DEFAULT_TEXT_COLOR));
          applyFont(nameTextView, typedArray);
        } else {
          nameTextView.setVisibility(View.GONE);
          titleInsideLayout.setVisibility(View.VISIBLE);

          int titleTextSize = typedArray.getDimensionPixelSize(R.styleable.StoryView_title_text_size, 32);
          tvTitleInside.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
          tvTitleInside.setText(stories.get(position).getTitle());
          tvTitleInside.setTextColor(typedArray.getColor(R.styleable.StoryView_text_color, DEFAULT_TEXT_COLOR));
          applyFont(tvTitleInside, typedArray);

          RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tvTitleInside.getLayoutParams();
          layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
          layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);

          if (titlePosition == 1) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
          } else if (titlePosition == 2) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
          }

          tvTitleInside.setLayoutParams(layoutParams);
        }
      } else {
        nameTextView.setVisibility(View.GONE);
        titleInsideLayout.setVisibility(View.GONE);
      }

      loadImage(position, image);

      if (cornerRadius != -1) {
        cardView.setRadius(cornerRadius);
        cardViewShadow.setRadius(cornerRadius + 3);
      }

      if (iconSpace != -1) {
        ViewGroup.MarginLayoutParams parentLayoutParams = (ViewGroup.MarginLayoutParams) parentLayout.getLayoutParams();
        parentLayoutParams.setMargins(parentLayoutParams.leftMargin, parentLayoutParams.topMargin, (int) iconSpace, parentLayoutParams.bottomMargin);
        parentLayout.setLayoutParams(parentLayoutParams);
      }

      if (iconShadow && borderVisibility == 0) {
        applyIconBorder(position, borderLayout, cornerRadius, borderWidth, borderMargin, imageLayout);
        cardView.setCardElevation(15);
        cardView.setMaxCardElevation(15);
      } else if (borderVisibility == 0) {
        applyIconBorder(position, borderLayout, cornerRadius, borderWidth, borderMargin, imageLayout);
      } else if (iconShadow) {
        cardView.setCardElevation(15);
        cardView.setMaxCardElevation(15);
      }

      image.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if (onItemClickListener == null) {
            return;
          }
          onItemClickListener.onClicked(position);
        }
      });
    } catch (Exception e) {
      Logger.e(TAG, "Error in onBindViewHolder of StoryViewListAdapter", e);
    }
  }

  public void applyIconBorder(int position, LinearLayout borderLayout, float cornerRadius, int borderWidth, float borderMargin, LinearLayout imageLayout) {
    try {
      if (stories.get(position).isOpened()) {
        borderLayout.setBackground(null);
      } else {
        GradientDrawable border = new GradientDrawable();
        if (cornerRadius == -1) {
          // No corner radius provided, display as a circle
          border.setShape(GradientDrawable.OVAL);
          border.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
        } else {
          // Set the corner radius
          border.setCornerRadius(cornerRadius);
        }
        border.setColor(0xFFFFFFFF); //white background
        border.setStroke(borderWidth, typedArray.getColor(R.styleable.StoryView_border_color, DEFAULT_BORDER_COLOR)); //black border with full opacity
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
          borderLayout.setBackgroundDrawable(border);
        } else {
          borderLayout.setBackground(border);
        }

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) imageLayout.getLayoutParams();
        params.setMargins((int) borderMargin, (int) borderMargin, (int) borderMargin, (int) borderMargin);
        imageLayout.setLayoutParams(params);
      }
    } catch (Exception e) {
      Logger.e(TAG, "Error while applying border to icon. " + e.getLocalizedMessage(), e);
    }
  }

  public static int convertDpToPx(Context context, int dp) {
    float density = context.getResources().getDisplayMetrics().density;
    return Math.round(dp * density);
  }

  @Override
  public int getItemCount() {
    return stories.size();
  }

  private void loadImage(int position, ImageView image) {
    try {
      ActivityLifecycleListener.currentActivity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          String imageUrl = stories.get(position).getContent().getPreview().getPosterPortraitSrc();

          RequestOptions options = new RequestOptions()
                  .fitCenter()
                  .placeholder(R.drawable.ic_story_placeholder)
                  .error(R.drawable.ic_story_placeholder)
                  .priority(Priority.HIGH);

          Glide.with(context)
                  .load(imageUrl)
                  .apply(options)
                  .into(image);
        }
      });
    } catch (Exception exception) {
      Logger.e(TAG, "Error while loading image in StoryViewListAdapter", exception);
    }
  }

  public static Bitmap getRoundedCroppedBitmap(Bitmap bitmap, int radius) {
    Bitmap finalBitmap;
    if (bitmap.getWidth() != radius || bitmap.getHeight() != radius) {
      finalBitmap = Bitmap.createScaledBitmap(bitmap, radius, radius, false);

    } else {
      finalBitmap = bitmap;
    }
    Bitmap output = Bitmap.createBitmap(finalBitmap.getWidth(),
        finalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);

    final Paint paint = new Paint();
    final Rect rect = new Rect(0, 0, finalBitmap.getWidth(),
        finalBitmap.getHeight());

    paint.setAntiAlias(true);
    paint.setFilterBitmap(true);
    paint.setDither(true);
    canvas.drawARGB(0, 0, 0, 0);
    paint.setColor(Color.parseColor("#BAB399"));
    canvas.drawCircle(finalBitmap.getWidth() / 2 + 0.7f,
        finalBitmap.getHeight() / 2 + 0.7f,
        finalBitmap.getWidth() / 2 + 0.1f, paint);
    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    canvas.drawBitmap(finalBitmap, rect, rect, paint);
    return output;
  }

  /**
   * Applies a font to a TextView that uses the "fontPath" attribute.
   *
   * @param textView   TextView when the font should apply
   * @param typedArray Attributes that contain the "fontPath" attribute with the path to the font file in the assets folder
   */
  public void applyFont(TextView textView, TypedArray typedArray) {
    if (typedArray != null) {
      Context context = textView.getContext();
      String fontPath = typedArray.getString(R.styleable.StoryView_font_family);
      if (!TextUtils.isEmpty(fontPath)) {
        Typeface typeface = getTypeface(context, fontPath);
        if (typeface != null) {
          textView.setTypeface(typeface);
        }
      }
    }
  }

  /**
   * Gets a Typeface from the cache. If the Typeface does not exist, creates it, cache it and returns it.
   *
   * @param context a Context
   * @param path    Path to the font file in the assets folder. ie "fonts/MyCustomFont.ttf"
   * @return the corresponding Typeface (font)
   * @throws RuntimeException if the font asset is not found
   */
  public Typeface getTypeface(Context context, String path) throws RuntimeException {
    Typeface typeface;
    try {
      typeface = FontUtils.findFont(context, path);
    } catch (RuntimeException exception) {
      String message = "Font assets/" + path + " cannot be loaded";
      throw new RuntimeException(message);
    }
    return typeface;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public int getItemViewType(int position) {
    return position;
  }

  public void updateStories(ArrayList<Story> stories) {
    this.stories = stories;
    notifyDataSetChanged();
  }

  public static void setStoryViewListAdapter(StoryViewListAdapter storyViewAdapter) {
    storyViewListAdapter = storyViewAdapter;
  }

  public static StoryViewListAdapter getStoryViewListAdapter() {
    return storyViewListAdapter;
  }
}
