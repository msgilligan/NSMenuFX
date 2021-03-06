package de.codecentric.centerdevice;

import com.sun.javafx.scene.control.GlobalMenuAdapter;
import com.sun.javafx.stage.StageHelper;
import de.codecentric.centerdevice.dialogs.about.AboutStageBuilder;
import de.codecentric.centerdevice.glass.AdapterContext;
import de.codecentric.centerdevice.glass.GlassAdaptionException;
import de.codecentric.centerdevice.glass.MacApplicationAdapter;
import de.codecentric.centerdevice.glass.TKSystemMenuAdapter;
import de.codecentric.centerdevice.icns.IcnsParser;
import de.codecentric.centerdevice.icns.IcnsType;
import de.codecentric.centerdevice.labels.LabelMaker;
import de.codecentric.centerdevice.labels.LabelName;
import de.codecentric.centerdevice.listener.MenuBarSyncListener;
import de.codecentric.centerdevice.listener.WindowMenuUpdateListener;
import de.codecentric.centerdevice.util.MenuBarUtils;
import de.codecentric.centerdevice.util.StageUtils;
import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class MenuToolkit {
	private static final String APP_NAME = "Apple";
	private static final String DEFAULT_APP_ICON =
			"/System/Library/CoreServices/CoreTypes.bundle/Contents/Resources/GenericApplicationIcon.icns";

	private final TKSystemMenuAdapter systemMenuAdapter;
	private final MacApplicationAdapter applicationAdapter;

	private final LabelMaker labelMaker;

	private MenuToolkit(AdapterContext adapterContext, LabelMaker labelMaker) {
		this.systemMenuAdapter = adapterContext.getSystemMenuAdapter();
		this.applicationAdapter = adapterContext.getApplicationAdapter();
		this.labelMaker = labelMaker;
	}

	public static MenuToolkit toolkit() {
		return toolkit(Locale.ENGLISH);
	}

	public static MenuToolkit toolkit(Locale locale) {
		return toolkit(new LabelMaker(locale));
	}

	public static MenuToolkit toolkit(LabelMaker labelMaker) {
		AdapterContext context = AdapterContext.getContext();
		if (context == null) {
			return null;
		}

		return new MenuToolkit(context, labelMaker);
	}

	public Menu createDefaultApplicationMenu(String appName) {
		return new Menu(APP_NAME, null, createAboutMenuItem(appName), new SeparatorMenuItem(), createHideMenuItem(appName), createHideOthersMenuItem(),
				createUnhideAllMenuItem(), new SeparatorMenuItem(), createQuitMenuItem(appName));
	}

	public MenuItem createAboutMenuItem(String appName) {
		MenuItem about = new MenuItem(labelMaker.getLabel(LabelName.ABOUT, appName));
		AboutStageBuilder stageBuilder = AboutStageBuilder.start(labelMaker.getLabel(LabelName.ABOUT, appName))
				.withAppName(appName).withCloseOnFocusLoss().withCopyright("Copyright \u00A9 " + Calendar
						.getInstance().get(Calendar.YEAR));

		try {
			IcnsParser parser = IcnsParser.forFile(DEFAULT_APP_ICON);
			stageBuilder.withImage(new Image(parser.getIconStream(IcnsType.ic08)));
		} catch (IOException e) {
			// Too bad, cannot load dummy image
		}

		Stage aboutStage = stageBuilder.build();
		about.setOnAction(event -> aboutStage.show());
		return about;
	}

	public MenuItem createQuitMenuItem(String appName) {
		MenuItem quit = new MenuItem(labelMaker.getLabel(LabelName.QUIT, appName));
		quit.setOnAction(event -> applicationAdapter.quit());
		quit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN));
		return quit;
	}

	public MenuItem createUnhideAllMenuItem() {
		MenuItem unhideAll = new MenuItem(labelMaker.getLabel(LabelName.SHOW_ALL));
		unhideAll.setOnAction(event -> applicationAdapter.unhideAllApplications());
		return unhideAll;
	}

	public MenuItem createHideOthersMenuItem() {
		MenuItem hideOthers = new MenuItem(labelMaker.getLabel(LabelName.HIDE_OTHERS));
		hideOthers.setOnAction(event -> applicationAdapter.hideOtherApplications());
		hideOthers.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.META_DOWN, KeyCombination.ALT_DOWN));
		return hideOthers;
	}

	public MenuItem createHideMenuItem(String appName) {
		MenuItem hide = new MenuItem(labelMaker.getLabel(LabelName.HIDE, appName));
		hide.setOnAction(event -> applicationAdapter.hide());
		hide.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.META_DOWN));
		return hide;
	}

	public MenuItem createMinimizeMenuItem() {
		MenuItem menuItem = new MenuItem(labelMaker.getLabel(LabelName.MINIMIZE));
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.META_DOWN));
		menuItem.setOnAction(event -> StageUtils.minimizeFocusedStage());
		return menuItem;
	}

	public MenuItem createZoomMenuItem() {
		MenuItem menuItem = new MenuItem(labelMaker.getLabel(LabelName.ZOOM));
		menuItem.setOnAction(event -> StageUtils.zoomFocusedStage());
		return menuItem;
	}

	public MenuItem createCloseWindowMenuItem() {
		MenuItem menuItem = new MenuItem(labelMaker.getLabel(LabelName.CLOSE_WINDOW));
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.META_DOWN));
		menuItem.setOnAction(event -> StageUtils.closeCurrentStage());
		return menuItem;
	}

	public MenuItem createBringAllToFrontItem() {
		MenuItem menuItem = new MenuItem(labelMaker.getLabel(LabelName.BRING_ALL_TO_FRONT));
		menuItem.setOnAction(event -> StageUtils.bringAllToFront());
		return menuItem;
	}

	public MenuItem createCycleWindowsItem() {
		MenuItem menuItem = new MenuItem(labelMaker.getLabel(LabelName.CYCLE_THROUGH_WINDOWS));
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.BACK_QUOTE, KeyCombination.META_DOWN));
		menuItem.setOnAction(event -> StageUtils.focusNextStage());
		return menuItem;
	}

	public void setApplicationMenu(Menu menu) {
		try {
			systemMenuAdapter.setAppleMenu(GlobalMenuAdapter.adapt(menu));
		} catch (Throwable e) {
			throw new GlassAdaptionException(e);
		}
	}

	public void setGlobalMenuBar(MenuBar menuBar) {
		setMenuBar(menuBar);
		MenuBarSyncListener.register(menuBar);
	}

	public void unsetGlobalMenuBar() {
		MenuBarSyncListener.unregister();
	}

	public void setMenuBar(MenuBar menuBar) {
		StageHelper.getStages().forEach(stage -> setMenuBar(stage, menuBar));
	}

	public void setMenuBar(Stage stage, MenuBar menuBar) {
		Parent parent = stage.getScene().getRoot();
		if (parent instanceof Pane) {
			setMenuBar((Pane) parent, menuBar);
		}
	}

	public void setMenuBar(Pane pane, MenuBar menuBar) {
		setApplicationMenu(extractApplicationMenu(menuBar));
		MenuBarUtils.setMenuBar(pane, menuBar);
	}

	public void autoAddWindowMenuItems(Menu menu) {
		menu.getItems().add(new SeparatorMenuItem());
		StageHelper.getStages().addListener(new WindowMenuUpdateListener(menu));
	}

	protected Menu extractApplicationMenu(MenuBar menuBar) {
		return menuBar.getMenus().get(0);
	}
}
