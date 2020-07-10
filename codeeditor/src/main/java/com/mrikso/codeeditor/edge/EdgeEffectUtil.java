package com.mrikso.codeeditor.edge;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Build.VERSION;
import android.view.View;
import android.widget.EdgeEffect;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.core.widget.EdgeEffectCompat;

/**
 * 一个工具类,用于辅助绘制效果
 *
 * @author Liujin 2018-10-11:9:56
 */
public class EdgeEffectUtil {

      /**
       * 四条边效果
       */
      private EdgeEffect mTopEffect;
      private EdgeEffect mBottomEffect;
      private EdgeEffect mLeftEffect;
      private EdgeEffect mRightEffect;

      /**
       * 效果尺寸
       */
      private int mWidth;
      private int mHeight;

      /**
       * 需要绘制效果的view
       */
      private View mView;

      /**
       * 是否已经释放
       */
      private boolean isReleased;

      /**
       * @param view 需要效果的view
       */
      @SuppressWarnings("SuspiciousNameCombination")
      public EdgeEffectUtil ( View view ) {

            Context context = view.getContext();

            mTopEffect = new EdgeEffect( context );
            mBottomEffect = new EdgeEffect( context );
            mLeftEffect = new EdgeEffect( context );
            mRightEffect = new EdgeEffect( context );

            mView = view;
      }

      @SuppressWarnings("SuspiciousNameCombination")
      public void setSize ( int width, int height ) {

            /* 设置效果范围 */
            mTopEffect.setSize( width, height );
            mBottomEffect.setSize( width, height );
            mLeftEffect.setSize( height, width );
            mRightEffect.setSize( height, width );

            mWidth = width;
            mHeight = height;
      }

      /**
       * 在{@link #mView#onDraw(Canvas)}中回调,绘制效果
       */
      public void onDraw ( Canvas canvas ) {

            canvas.save();
            mTopEffect.draw( canvas );
            canvas.restore();

            canvas.save();
            canvas.translate( mWidth, mHeight );
            canvas.rotate( -180 );
            mBottomEffect.draw( canvas );
            canvas.restore();

            canvas.save();
            canvas.translate( 0, mHeight );
            canvas.rotate( -90 );
            mLeftEffect.draw( canvas );
            canvas.restore();

            canvas.save();
            canvas.translate( mWidth, 0 );
            canvas.rotate( 90 );
            mRightEffect.draw( canvas );
            canvas.restore();

            if( isReleased ) {

                  boolean isInvalidate = false;
                  if( !mTopEffect.isFinished() ) {
                        isInvalidate = true;
                  }
                  if( !mBottomEffect.isFinished() ) {
                        isInvalidate = true;
                  }
                  if( !mLeftEffect.isFinished() ) {
                        isInvalidate = true;
                  }
                  if( !mRightEffect.isFinished() ) {
                        isInvalidate = true;
                  }
                  if( isInvalidate ) {
                        mView.invalidate();
                  } else {
                        isReleased = false;
                  }
            }
      }

      /**
       * 回弹
       */
      public void releaseAllEdge ( ) {

            boolean call = false;
            /* 回弹 */
            if( !mTopEffect.isFinished() ) {
                  mTopEffect.onRelease();
                  call = true;
            }
            if( !mBottomEffect.isFinished() ) {
                  mBottomEffect.onRelease();
                  call = true;
            }
            if( !mLeftEffect.isFinished() ) {
                  mLeftEffect.onRelease();
                  call = true;
            }
            if( !mRightEffect.isFinished() ) {
                  mRightEffect.onRelease();
                  call = true;
            }
            isReleased = true;
            /* 触发重绘 */
            if( call ) {
                  mView.invalidate();
            }
      }

      public void releaseLeft ( ) {

            if( !mLeftEffect.isFinished() ) {
                  mLeftEffect.onRelease();
                  mView.invalidate();
                  isReleased = true;
            }
      }

      public void releaseTop ( ) {

            if( !mTopEffect.isFinished() ) {
                  mTopEffect.onRelease();
                  mView.invalidate();
                  isReleased = true;
            }
      }

      public void releaseRight ( ) {

            if( !mRightEffect.isFinished() ) {
                  mRightEffect.onRelease();
                  mView.invalidate();
                  isReleased = true;
            }
      }

      public void releaseBottom ( ) {

            if( !mBottomEffect.isFinished() ) {
                  mBottomEffect.onRelease();
                  mView.invalidate();
                  isReleased = true;
            }
      }

      /**
       * 强制结束
       */
      public void forceStop ( ) {

            mLeftEffect.onPull( 0 );
            mTopEffect.onPull( 0 );
            mRightEffect.onPull( 0 );
            mBottomEffect.onPull( 0 );
            mView.invalidate();
      }

      /**
       * 在需要左边效果的时候调用
       *
       * @param deltaDistanceX 0时没有效果,1时满效果
       */
      public void pullLeft ( @FloatRange(from = 0, to = 1) float deltaDistanceX ) {

            mLeftEffect.onPull( deltaDistanceX );
            mView.invalidate();
      }

      /**
       * 在需要左边效果的时候调用
       *
       * @param deltaDistanceX 0时没有效果,1时满效果
       */
      public void pullLeft (
          @FloatRange(from = 0, to = 1) float deltaDistanceX,
          @FloatRange(from = 0, to = 1) float displacement ) {

            EdgeEffectCompat.onPull( mBottomEffect, deltaDistanceX, displacement );
            mView.invalidate();
      }

      /**
       * 在需要上边效果的时候调用
       *
       * @param deltaDistanceY 0时没有效果,1时满效果
       */
      public void pullTop ( @FloatRange(from = 0, to = 1) float deltaDistanceY ) {

            mTopEffect.onPull( deltaDistanceY );
            mView.invalidate();
      }

      /**
       * 在需要上边效果的时候调用
       *
       * @param deltaDistanceY 0时没有效果,1时满效果
       */
      public void pullTop (
          @FloatRange(from = 0, to = 1) float deltaDistanceY,
          @FloatRange(from = 0, to = 1) float displacement ) {

            EdgeEffectCompat.onPull( mTopEffect, deltaDistanceY, displacement );
            mView.invalidate();
      }

      /**
       * 在需要右边效果的时候调用
       *
       * @param deltaDistanceX 0时没有效果,1时满效果
       */
      public void pullRight ( @FloatRange(from = 0, to = 1) float deltaDistanceX ) {

            mRightEffect.onPull( deltaDistanceX );
            mView.invalidate();
      }

      /**
       * 在需要右边效果的时候调用
       *
       * @param deltaDistanceX 0时没有效果,1时满效果
       */
      public void pullRight (
          @FloatRange(from = 0, to = 1) float deltaDistanceX,
          @FloatRange(from = 0, to = 1) float displacement ) {

            EdgeEffectCompat.onPull( mRightEffect, deltaDistanceX, displacement );
            mView.invalidate();
      }

      /**
       * 在需要下边效果的时候调用
       *
       * @param deltaDistanceY 0时没有效果,1时满效果
       */
      public void pullBottom ( @FloatRange(from = 0, to = 1) float deltaDistanceY ) {

            mBottomEffect.onPull( deltaDistanceY );
            mView.invalidate();
      }

      /**
       * 在需要下边效果的时候调用
       *
       * @param deltaDistanceY 0时没有效果,1时满效果
       */
      public void pullBottom (
          @FloatRange(from = 0, to = 1) float deltaDistanceY,
          @FloatRange(from = 0, to = 1) float displacement ) {

            EdgeEffectCompat.onPull( mBottomEffect, deltaDistanceY, displacement );
            mView.invalidate();
      }

      /**
       * 设置颜色
       */
      public void setColor ( @ColorInt int color ) {

            if( VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                  mLeftEffect.setColor( color );
                  mTopEffect.setColor( color );
                  mRightEffect.setColor( color );
                  mBottomEffect.setColor( color );
            }
      }
}
