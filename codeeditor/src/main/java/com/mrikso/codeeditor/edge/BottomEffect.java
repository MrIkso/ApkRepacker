package com.mrikso.codeeditor.edge;

import android.graphics.Canvas;
import android.view.View;

/**
 * @author Liujin 2018-11-07:16:17
 */
public class BottomEffect extends BaseEdgeEffect {

      /**
       * @param view 需要效果的view
       */
      @SuppressWarnings("SuspiciousNameCombination")
      public BottomEffect ( View view ) {

            super( view );
      }

      @Override
      @SuppressWarnings("SuspiciousNameCombination")
      public void setSize ( int width, int height ) {

            /* 设置效果范围 */
            super.setSize( width, height );
            mEffect.setSize( width, height );
      }

      /**
       * 在{@link #mView#onDraw(Canvas)}中回调,绘制效果
       */
      @Override
      public void onDraw ( Canvas canvas ) {

            canvas.save();
            canvas.translate( mWidth, mHeight );
            canvas.rotate( -180 );
            mEffect.draw( canvas );
            canvas.restore();

            super.onDraw( canvas );
      }
}
