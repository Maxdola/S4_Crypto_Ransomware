package de.dhbw.report;

public class ReportManger {

  private ReportManger instance;

  private IReportGenerator reportGenerator;

  public ReportManger() {
    if (instance == null) instance = this;
    this.reportGenerator = new ReportGenerator();
  }

  public IReportGenerator getReportGenerator() {
    return reportGenerator;
  }

  public ReportManger getInstance() {
    return instance;
  }
}
