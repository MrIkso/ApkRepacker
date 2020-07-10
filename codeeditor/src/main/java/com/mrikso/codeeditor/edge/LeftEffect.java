package com.mrikso.codeeditor.edge;

import android.graphics.Canvas;
import android.view.View;

/**
 * @author Liujin 2018-11-07:16:17
 */
public class LeftEffect extends BaseEdgeEffect {

      /**
       * @param view 需要效果的view
       */
      @SuppressWarnings("SuspiciousNameCombination")
      public LeftEffect ( View view ) {

            super( view );
      }

      @Override
      @SuppressWarnings("SuspiciousNameCombination")
      public void setSize ( int width, int height ) {

            /* 设置效果范围 */
            mEffect.setSize( height, width );
            mWidth = width;
            mHeight = height;
      }

      /**
       * 在{@link #mView#onDraw(Canvas)}中回调,绘制效果
       */
      @Override
      public void onDraw ( Canvas canvas ) {

            canvas.save();
            canvas.translate( 0, mHeight );
            canvas.rotate( -90 );
            mEffect.draw( canvas );
            canvas.restore();

            super.onDraw( canvas );
      }
}
