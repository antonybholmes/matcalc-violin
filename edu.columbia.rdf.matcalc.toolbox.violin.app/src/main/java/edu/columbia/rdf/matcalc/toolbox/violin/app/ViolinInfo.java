package edu.columbia.rdf.matcalc.toolbox.violin.app;

import org.jebtk.core.AppVersion;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.help.GuiAppInfo;

public class ViolinInfo extends GuiAppInfo {

  public ViolinInfo() {
    super("Violin", new AppVersion(1),
        "Copyright (C) ${year} Antony Holmes",
        AssetService.getInstance().loadIcon(ViolinIcon.class, 32),
        AssetService.getInstance().loadIcon(ViolinIcon.class, 128));
  }

}
