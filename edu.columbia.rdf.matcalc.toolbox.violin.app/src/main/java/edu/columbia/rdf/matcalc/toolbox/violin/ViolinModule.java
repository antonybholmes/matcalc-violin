package edu.columbia.rdf.matcalc.toolbox.violin;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.jebtk.core.ColorUtils;
import org.jebtk.core.collections.UniqueArrayList;
import org.jebtk.core.io.FileUtils;
import org.jebtk.core.io.PathUtils;
import org.jebtk.core.io.TempService;
import org.jebtk.core.settings.SettingsService;
import org.jebtk.core.sys.ExternalProcess;
import org.jebtk.graphplot.figure.series.XYSeries;
import org.jebtk.graphplot.figure.series.XYSeriesGroup;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.dialog.ModernDialogStatus;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.ribbon.RibbonLargeButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.toolbox.CalcModule;
import edu.columbia.rdf.matcalc.toolbox.violin.app.ViolinIcon;

public class ViolinModule extends CalcModule
implements ModernClickListener {

  // private static final int DEFAULT_POINTS =
  // SettingsService.getInstance().getInt("pattern-discovery.cdf.points");

  // private static final List<Double> EVAL_POINTS =
  // Linspace.generate(0, 1, DEFAULT_POINTS);

  private static Path SCRIPT = 
      SettingsService.getInstance().getFile("violin.script.path"); //PathUtils.getPath("res/scripts/python/violin.py");

  private MainMatCalcWindow mWindow;

  private static final Logger LOG = LoggerFactory
      .getLogger(ViolinModule.class);

  private static final String PYTHON = 
      SettingsService.getInstance().getString("violin.python.interpreter");

  @Override
  public String getName() {
    return "Violin";
  }

  @Override
  public void init(MainMatCalcWindow window) {
    mWindow = window;

    RibbonLargeButton button = new RibbonLargeButton("Violin",
        AssetService.getInstance().loadIcon(ViolinIcon.class, 24),
        "Violin Plot", "Create violin plots.");
    button.addClickListener(this);
    mWindow.getRibbon().getToolbar("Plot").getSection("Plot")
    .add(button);
  }

  @Override
  public void clicked(ModernClickEvent e) {
    try {
      violin();
    } catch (IOException | InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Pattern discovery.
   *
   * @param properties the properties
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InterruptedException 
   */
  private void violin() throws IOException, InterruptedException {
    XYSeriesGroup groups = mWindow.getRowGroups();

    if (groups.getCount() == 0) {
      MainMatCalcWindow.createGroupWarningDialog(mWindow);

      return;
    }

    DataFrame m = mWindow.getCurrentMatrix();

    if (m == null) {
      return;
    }

    List<String> labels = new UniqueArrayList<String>();

    for (int i = 0; i < m.getRows(); ++i) {
      labels.add(m.getRowName(i));
    }

    ViolinDialog dialog = new ViolinDialog(mWindow, labels);

    dialog.setVisible(true);

    if (dialog.getStatus() == ModernDialogStatus.CANCEL) {
      return;
    }

    Path in = TempService.getInstance().generateTmpFile("txt");

    BufferedWriter writer = FileUtils.newBufferedWriter(in);

    try {
      writer.write("Label\tValue");
      writer.newLine();
      
      for (XYSeries group : groups) {
        List<Integer> indices = XYSeries.findRowIndices(m, group);
      
        for (int i : indices) {
          writer.write(m.getRowName(i) + "\t" + m.getText(i, 0));
          writer.newLine();
        }
      }
    } finally {
      writer.close();
    }


    // Make the colors file

    Path colorFile = TempService.getInstance().generateTmpFile("txt");

    writer = FileUtils.newBufferedWriter(colorFile);

    try {
      writer.write("Color");
      writer.newLine();
      for (XYSeries group : groups) {
        writer.write(ColorUtils.toHtml(group.getColor()));
        writer.newLine();
      }
    } finally {
      writer.close();
    }

    // First make a table for seaborn

    Path out = TempService.getInstance().generateTmpFile("violin", "pdf");

    ExternalProcess p = new ExternalProcess(TempService.getInstance().getTmpDir());
    p.addArg(PYTHON,
        PathUtils.toString(SCRIPT), 
        PathUtils.toString(in), 
        PathUtils.toString(colorFile), 
        PathUtils.toString(out));
    p.addParam("xlabel", dialog.getXLabel());
    p.addParam("ylabel", dialog.getYLabel());
    p.addParam("violinplot", dialog.getShowViolin());
    p.addParam("boxplot", dialog.getShowBox());
    p.addParam("swarmplot", dialog.getShowSwarm());

    System.err.println(p.toString());
    
    p.run();
    
    Desktop.getDesktop().open(out.toFile());
  }
}
