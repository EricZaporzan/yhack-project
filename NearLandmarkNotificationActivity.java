package com.microsoft.band.sdk.sampleapp;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.notifications.MessageFlags;
import com.microsoft.band.sdk.sampleapp.notification.R;

import com.microsoft.band.tiles.BandTile;
import com.microsoft.band.tiles.pages.ScrollFlowPanel;
import com.microsoft.band.tiles.pages.FlowPanelOrientation;
import com.microsoft.band.tiles.pages.PageData;
import com.microsoft.band.tiles.pages.PageLayout;
import com.microsoft.band.tiles.pages.PageTextBlockData;
import com.microsoft.band.tiles.pages.TextBlock;
import com.microsoft.band.tiles.pages.TextBlockFont;
import com.microsoft.band.tiles.pages.ElementColorSource

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NearLandmarkNotificationActivity extends Activity {
  private BandClient client = null;
  private Button btnStart;
  private TextView txtStatus;

  private UUID tileId = UUID.randomUUID();

  enum TileLayoutIndex {
    LandmarkLayout
  }

  enum TileMessagesPageElementId {
    TITLE = 1,
    DESCRIPTION
  }

  ScrollFlowPanel panel1 = new ScrollFlowPanel(new PageRect(0, 0, 245, 102));
  panel1.setHorizontalAlignment(PageHorizontalAlignment.LEFT);
  panel1.setVerticalAlignment(PageVerticalAlignment.TOP);

  WrappedTextBlock titleBlock = new WrappedTextBlock(new PageRect(0, 0, 245, 102), WrappedTextBlockFont.LARGE);
  textBlock1.setId(TileMessagesPageElementId.TITLE.ordinal());
  textBlock1.setMargins(new PageMargin(15, 0, 15, 0));
  textBlock1.setColor(Color.WHITE);
  textBlock1.setAutoHeightEnabled(true);
  textBlock1.setHorizontalAlignment(PageHorizontalAlignment.LEFT);
  textBlock1.setVerticalAlignment(PageVerticalAlignment.TOP);

  WrappedTextBlock descBlock = new WrappedTextBlock(new PageRect(0, 0,
245, 102), WrappedTextBlockFont.SMALL);
  textBlock2.setId(TileMessagesPageElementId.DESCRIPTION.ordinal());
  textBlock2.setMargins(new PageMargin(15, 0, 15, 0));
  textBlock2.setColorSource(ElementColorSource.BAND_BASE);
  textBlock2.setAutoHeightEnabled(true);
  textBlock2.setHorizontalAlignment(PageHorizontalAlignment.LEFT);
  textBlock2.setVerticalAlignment(PageVerticalAlignment.TOP);

  panel1.addElements(titleBlock, descBlock);
  PageLayout layout = new PageLayout(panel1);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    txtStatus = (TextView) findViewById(R.id.txtStatus);

    btnStart = (Button) findViewById(R.id.btnStart);
    btnStart.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        txtStatus.setText("");
        new appTask().execute();
      }
    });
  }

  @Override
  protected void onDestroy() {
    if (client != null) {
      try {
        client.disconnect().await();
      } catch (InterruptedException e) {
        // Do nothing as this is happening during destroy
      } catch (BandException e) {
        // Do nothing as this is happening during destroy
      }
    }
    super.onDestroy();
  }

  private class appTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (getConnectedBandClient()) {
          if(client.getTileManager().setPages(tileId, new PageData(TilePageId.PAGE1.Uuid,
          TileLayoutIndex.MESSAGES_LAYOUT.ordinal())
          .update(new PageWrappedTextBlockData(TileMessagesPageElementId.TITLE.ordinal(),
          "This is the text of the first message"))
          .update(new PageWrappedTextBlockData(TileMessagesPageElementId.DESCRIPTION.ordinal(),
          "This is the text of the second message"))).await()) {
            // handle set pages failure
          }
				} else {

			} catch (BandException e) {
				String exceptionMessage="";
				switch (e.getErrorType()) {
				case DEVICE_ERROR:
					exceptionMessage = "Please make sure bluetooth is on and the band is in range.\n";
					break;
				case UNSUPPORTED_SDK_VERSION_ERROR:
					exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
					break;
				case SERVICE_ERROR:
					exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
					break;
				case BAND_FULL_ERROR:
					exceptionMessage = "Band is full. Please use Microsoft Health to remove a tile.\n";
					break;
				default:
					exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
					break;
				}

			} catch (Exception e) {

			}
			return null;
		}
	}

  private boolean doesTileExist(List<BandTile> tiles, UUID tileId) {
		for (BandTile tile:tiles) {
			if (tile.getTileId().equals(tileId)) {
				return true;
			}
		}
		return false;
	}

  private boolean addTile() throws Exception {
    /* Set the options */
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inScaled = false;
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    Bitmap tileIcon = BitmapFactory.decodeResource(getBaseContext().getResources(), R.raw.tile_icon_large, options);
    Bitmap badgeIcon = BitmapFactory.decodeResource(getBaseContext().getResources(), R.raw.tile_icon_small, options);

		BandTile tile = new BandTile.Builder(tileId, "MessageTile", tileIcon)
			.setTileSmallIcon(badgeIcon).build();
		if (client.getTileManager().addTile(this, tile).await()) {
			return true;
		} else {
			return false;
		}
	}

  private boolean getConnectedBandClient() throws InterruptedException, BandException {
		if (client == null) {
			BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
			if (devices.length == 0) {
				return false;
			}
			client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
		} else if (ConnectionState.CONNECTED == client.getConnectionState()) {
			return true;
		}

		return ConnectionState.CONNECTED == client.connect().await();
	}
}
