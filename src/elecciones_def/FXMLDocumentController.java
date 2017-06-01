/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elecciones_def;

import electionresults.model.ElectionResults;
import electionresults.model.PartyResults;
import electionresults.model.RegionResults;
import electionresults.persistence.io.DataAccessLayer;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import jdk.nashorn.internal.runtime.regexp.RegExpFactory;

/**
 *
 * @author ganimedes
 */
public class FXMLDocumentController implements Initializable {
    /// FXML made vars
    private Label label;
    @FXML
    private VBox formVbox;
    @FXML
    private ChoiceBox<String> provCB;
    @FXML
    private ChoiceBox<String> regCB;
    @FXML
    private Accordion acc;
    @FXML
    private TitledPane pane1;
    @FXML
    private HBox hb1;
    @FXML
    private HBox hb2; 
    @FXML
    private TitledPane pane2;
    @FXML
    private TitledPane pane3;
    @FXML
    private Pane hb3;
    @FXML
    private ProgressBar progress;
    @FXML
    private Label progressLabel;
      
    
    /// attrs
    int[] yearIndex  = {1995, 1999, 2003, 2007, 2011, 2015};
    List<ElectionResults> erList;
    int numberOfToasts = 0;
    @FXML
    private Slider slider;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        slider.setMax(5.0);
        slider.setBlockIncrement(1.0);
        slider.setDisable(true);
        
        
        // init autoField
        AutoCompleteTextField autoField = new AutoCompleteTextField();
        autoField.promptTextProperty().set("Año");                
        for(int i = 0; i<yearIndex.length;i++)
            autoField.getEntries().add(yearIndex[i]+"");        
        autoField.setMaxWidth(150);
        autoField.setPrefWidth(150);
        autoField.setDisable(true);
        formVbox.getChildren().add(0, autoField);
        
        autoField.setOnKeyPressed((event01)->{
            if(numberOfToasts==0){
                Toast toast = new Toast();            
                String toastMsg = "Recuerda pulsar [Enter] una vez has seleccionado el año";
                toast.makeText(Elecciones_def.stage, toastMsg, 2000, 500, 500);
                numberOfToasts++;
            }
            
        });
        
        // init several nodes
        pane2.setText("Distribución de votos");
        pane3.setText("Evolución de la participación");
        
        // load data in bg;
        DataManager bgTask = new DataManager();        
        new Thread(bgTask).start();        
        progress.progressProperty().bind(bgTask.progressProperty());
        progress.visibleProperty().bind(bgTask.runningProperty());        
        
        // loading data info in ui
        bgTask.setOnRunning((event0)->{
            progressLabel.setText("Cargando datos ...");
        });
        
        // if data is succefully loaded ...
        bgTask.setOnSucceeded((event)->{
            progressLabel.setText("");
            // instance erList
            erList = bgTask.valueProperty().get();
            
            // load charts that do not need input
            loadGeneralData(erList);
            
            
            // enable year field
            autoField.setDisable(false);    
            
            // if there's some year input ...
            autoField.setOnAction((event2)->{
                
                // instance eventual alerts
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Diálogo de error");
                alert.setHeaderText("Algo ha ocurrido");
                
                if(autoField.getText().length()>3){
                                        
                    Boolean badYear = true;
                    for (int i = 0; i < yearIndex.length; i++) 
                        if (Integer.parseInt(autoField.getText()) == yearIndex[i])
                            badYear = false;
                    if(!badYear){
                        
                        //slider event
                        slider.setDisable(false);
                        slider.valueProperty().addListener((event34) -> {
                            double val = slider.getValue();
                            Node[] nodes = new Node[hb2.getChildren().size()];
                            ElectionResults er = erList.get(yearToIndex(Integer.parseInt(autoField.getText())));
                            
                            for(int i = 0;i < hb2.getChildren().size(); i++){
                                nodes[i] = hb2.getChildren().get(i);
                                BarChart<String, Number> cb = (BarChart) nodes[i];
                                String id = cb.getId().substring(0, 1);
                                if(id.equals("c")){
                                    
                                    //hb2.getChildren().remove(0); 
                                    List<PartyResults> listPr = er.getGlobalResults().getPartyResultsSorted();
                                    for(int  j= 0;j < listPr.size(); j++){
                                        if(listPr.get(j).getPercentage() <= val){
                                            
                                            listPr.remove(j);
                                            
                                        }
                                    }
                                                                                                          
                                    if(hb2.getChildren().size()>1){
                                        hb2.getChildren().remove(0);
                                        hb2.getChildren().add(0, caBarResults(listPr, er.getYear()));
                                    }
                                    else{
                                        hb2.getChildren().remove(0, hb2.getChildren().size());
                                        hb2.getChildren().add(caBarResults(listPr, er.getYear()));
                                    } 
                                        
                                    
                                    
                                    
                                } else if (id.equals("p")){
                                    String prov = cb.getTitle().substring(25, cb.getTitle().length());
                                    List<PartyResults> listPr = er.getProvinceResults(prov).getPartyResultsSorted();
                                    for (int j = 0; j < listPr.size(); j++) {
                                        if (listPr.get(j).getPercentage() <= val) {

                                            listPr.remove(j);

                                        }
                                    }
                                    
                                      
                                        hb2.getChildren().remove(1);
                                        hb2.getChildren().add(1, provinceBarResults(listPr, prov, er.getYear()));
                                    
                                    
                                } else {
                                    String reg = cb.getTitle().substring(25, cb.getTitle().length());
                                    List<PartyResults> listPr = er.getRegionResults(reg).getPartyResultsSorted();
                                    for (int j = 0; j < listPr.size(); j++) {
                                        if (listPr.get(j).getPercentage() <= val) {

                                            listPr.remove(j);

                                        }
                                    }
                                    if(hb2.getChildren().size()==1){
                                        hb2.getChildren().remove(1);
                                        hb2.getChildren().add(1, regionBarResulst(listPr, reg, er.getYear()));
                                    }
                                    else {
                                        hb2.getChildren().remove(2);
                                        hb2.getChildren().add(2, regionBarResulst(listPr, reg, er.getYear()));
                                    }
                                }
                            }
                            
                            
                            
                        });

                        
                        // converting year to list index 
                        int year = Integer.parseInt(autoField.getText());                        
                        int j = yearToIndex(year);
                        
                       
                        // enable and populate - if needed - choiceboxes
                        provCB.setDisable(false);
                        regCB.setDisable(false);
                        if(provCB.getItems().isEmpty()){
                            provCB.setItems(FXCollections.observableArrayList(erList.get(j).getProvinces().keySet()));
                            regCB.setItems(FXCollections.observableArrayList(erList.get(j).getRegionProvinces().keySet()));
                        }
                        
                        // load charts that need the year input
                        loadYearData(erList, j);
                        
                        
                        provCB.setOnAction((event3)->{
                            
                            loadProvinceData(erList, provCB.getSelectionModel().getSelectedItem(),yearToIndex(Integer.parseInt(autoField.getText())));
                            
                        });
                        
                        regCB.setOnAction((event4)->{
                            
                            loadProvinceRegionData(erList, regCB.getSelectionModel().getSelectedItem(), yearToIndex(Integer.parseInt(autoField.getText())));
                        
                        });
                    }
                    else {
                        alert.setContentText("No han habido elecciones en el \n año introducido.");
                        alert.showAndWait();
                    }
                }
                else {
                    alert.setContentText("El año no puede quedar vacío si \n quieres cargar datos específicos");
                    alert.showAndWait();
                }
                    
            });
            
        });
        
        
    }

    
    public int yearToIndex(int year){
        int j = 0;
        for (int i = 0; i < yearIndex.length; i++) {
            if (year == yearIndex[i]) {
                j = i;
                break;
            }
        }
        
        return j;
    }

   

    public void loadGeneralData(List<ElectionResults> er){
        hb3.getChildren().add(lineChart(er));
        hb3.getChildren().add(stackedBarChart(er));
        hb3.getChildren().add(barChart(er));
    }    
    
    public void loadYearData(List<ElectionResults> er, int index){
        if(!hb1.getChildren().isEmpty())
            hb1.getChildren().remove(0, hb1.getChildren().size());
        
        if(!hb2.getChildren().isEmpty())
            hb2.getChildren().remove(1, hb2.getChildren().size());
        
        hb1.getChildren().add(0, caResults(er.get(index).getGlobalResults(), er.get(index).getYear()));
        hb2.getChildren().add(caBarResults(er.get(index).getGlobalResults(), er.get(index).getYear()));
        
    }
    
    public void loadProvinceData(List<ElectionResults> er, String prov, int index){
            if(hb1.getChildren().size()>1)
                hb1.getChildren().remove(1);
            hb1.getChildren().add( provinceResults(er.get(index).getProvinceResults(prov), prov));
           
            if(hb2.getChildren().size()==1){
                hb2.getChildren().add( provinceBarResults(er.get(index).getProvinceResults(prov), prov, index));        
            }
            
            
            if(hb2.getChildren().size()== 2 && hb2.getChildren().get(1).getId().equals("provBar")){
                hb2.getChildren().remove(1);
                hb2.getChildren().add(1, provinceBarResults(er.get(index).getProvinceResults(prov), prov, index));        
            }else if(hb2.getChildren().size()== 2 && !hb2.getChildren().get(1).getId().equals("provBar")){
                hb2.getChildren().remove(1);
                hb2.getChildren().add(1, provinceBarResults(er.get(index).getProvinceResults(prov), prov, index));
            }else if(hb2.getChildren().size()==3) {
                hb2.getChildren().remove(1);
                hb2.getChildren().add(1,provinceBarResults(er.get(index).getProvinceResults(prov), prov, index));
            }

    }
    
    
    public void loadProvinceRegionData(List<ElectionResults> er, String reg, int index){
        
        
        if(hb2.getChildren().size()==2 && hb2.getChildren().get(1).getId().equals("regBar")){
            hb2.getChildren().remove(1);
            hb2.getChildren().add(regionBarResults(er.get(index).getRegionResults(reg), reg, index));
        }
        else if(hb2.getChildren().size() == 2 && !hb2.getChildren().get(1).getId().equals("regBar")){
            hb2.getChildren().add(regionBarResults(er.get(index).getRegionResults(reg), reg, index));
            System.out.println("eres totonto");
        }
        
        if(hb2.getChildren().size()==3){
            hb2.getChildren().remove(2);
            hb2.getChildren().add(2,regionBarResults(er.get(index).getRegionResults(reg), reg, index));
        }
        
    }
    
    
    public PieChart caResults(RegionResults rg, int year){
        
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        rg.getPartyResultsSorted().forEach((results) -> {
            pieData.add(new PieChart.Data(results.getParty() + " (" + results.getSeats() + ")", results.getPercentage()));
        });
        PieChart pie = new PieChart();
        pie.setData(pieData);
        pie.setTitle("Escaños País Valencià (" + year+ ")");
        
        return pie;
    }       
    public PieChart provinceResults(RegionResults rg, String prov){
        
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        rg.getPartyResultsSorted().forEach((results) -> {
            pieData.add(new PieChart.Data(results.getParty() + " (" + results.getSeats() + ")", results.getPercentage()));
        });
        PieChart pie = new PieChart();
        pie.setData(pieData);
        pie.setTitle("Escaños de " + prov);
        
        return pie;
        
    }        
    
    public BarChart provinceBarResults(List<PartyResults> listPr, String prov, int year){
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> bChart = new BarChart<String, Number>(xAxis, yAxis);
            bChart.setTitle("Distribución de votos de " + prov);

            bChart.setId("provBar");

            int i = 0;
            Collection<XYChart.Series> bcData = FXCollections.observableArrayList();
            while (i < listPr.size()) {
                XYChart.Series series = new XYChart.Series();
                PartyResults pr = listPr.get(i);
                series.setName(pr.getParty());

                series.getData().add(new XYChart.Data(year + "", pr.getVotes()));
                bChart.getData().add(series);

                i++;
            }

            return bChart;
    }
    public BarChart provinceBarResults(RegionResults rg, String prov, int year){
        
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bChart = new BarChart<String, Number>(xAxis, yAxis);
        bChart.setTitle("Distribución de votos de " + prov);
                
        bChart.setId("provBar");
        
        int i = 0;
        Collection<XYChart.Series> bcData = FXCollections.observableArrayList();
        while (i<rg.getPartyResultsSorted().size()){
            XYChart.Series series = new XYChart.Series();
            PartyResults pr= rg.getPartyResultsSorted().get(i);
            series.setName(pr.getParty());
            
            series.getData().add(new XYChart.Data(year + "", pr.getVotes()));
            bChart.getData().add(series);
            
            
            i++;
        }
        
        
        
        return bChart;
    }            
    
    public BarChart regionBarResulst(List<PartyResults> listPr, String reg, int year){
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bChart = new BarChart<String, Number>(xAxis, yAxis);
        bChart.setTitle("Distribución de votos de " + reg);

        bChart.setId("regBar");

        int i = 0;
        Collection<XYChart.Series> bcData = FXCollections.observableArrayList();
        while (i < listPr.size()) {
            XYChart.Series series = new XYChart.Series();
            PartyResults pr = listPr.get(i);
            series.setName(pr.getParty());

            series.getData().add(new XYChart.Data(year + "", pr.getVotes()));
            bChart.getData().add(series);

            i++;
        }

        return bChart;
    }
    public BarChart regionBarResults(RegionResults rg, String reg, int year){
        
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bChart = new BarChart<String, Number>(xAxis, yAxis);
        bChart.setTitle("Distribución de votos de " + reg);
        
        bChart.setId("regBar");
        
        int i = 0;
        Collection<XYChart.Series> bcData = FXCollections.observableArrayList();
        while (i<rg.getPartyResultsSorted().size()){
            XYChart.Series series = new XYChart.Series();
            PartyResults pr= rg.getPartyResultsSorted().get(i);
            series.setName(pr.getParty());
            
            series.getData().add(new XYChart.Data(year +"", pr.getVotes()));
            bChart.getData().add(series);
            
            
            i++;
        }
        
        
        
        return bChart;
    }        
    
    public BarChart caBarResults(List<PartyResults> listPr, int year){
        
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bChart = new BarChart<String, Number>(xAxis, yAxis);
        bChart.setTitle("Distribución de votos del País Valencià en " + year);
        
        bChart.setId("caBar");
        
        
        int i = 0;
        Collection<XYChart.Series> bcData = FXCollections.observableArrayList();
        while (i<listPr.size()){
            XYChart.Series series = new XYChart.Series();
            PartyResults pr= listPr.get(i);
            series.setName(pr.getParty());
            
            series.getData().add(new XYChart.Data(year+ "", pr.getVotes()));            
            bChart.getData().add(series);                        
                        
            
            i++;
        }                                
        
        return bChart;
    }
    
    public BarChart caBarResults(RegionResults rg, int year){
        
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bChart = new BarChart<String, Number>(xAxis, yAxis);
        bChart.setTitle("Distribución de votos del País Valencià en " + year);
        
        bChart.setId("caBar");
        
        
        int i = 0;
        Collection<XYChart.Series> bcData = FXCollections.observableArrayList();
        while (i<rg.getPartyResultsSorted().size()){
            XYChart.Series series = new XYChart.Series();
            PartyResults pr= rg.getPartyResultsSorted().get(i);
            series.setName(pr.getParty());
            
            series.getData().add(new XYChart.Data(year+ "", pr.getVotes()));            
            bChart.getData().add(series);                        
                        
            
            i++;
        }                                
        
        return bChart;
    }    
    private StackedBarChart stackedBarChart(List<ElectionResults> er){
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        StackedBarChart<String, Number> sbChart = new StackedBarChart<>(xAxis, yAxis);
        sbChart.setTitle("Escaños del País Valencià ");
        
        int foo = 0;
        while(foo<er.size()){
            RegionResults rg = er.get(foo).getGlobalResults();
            
            int i = 0;
            Collection<XYChart.Series> bcData = FXCollections.observableArrayList();
            while (i < rg.getPartyResultsSorted().size()) {
                XYChart.Series series = new XYChart.Series();

                PartyResults pr = rg.getPartyResultsSorted().get(i);
               
                series.setName(pr.getParty());

                series.getData().add(new XYChart.Data(er.get(foo).getYear() + "", pr.getSeats()));                
                sbChart.getData().add(series);



                i++;
            }
            
            foo++;
        }
        sbChart.setLegendVisible(true);
        return sbChart;
    }
    private LineChart lineChart(List<ElectionResults> er){
        int i = 0;
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lChart = new LineChart<String, Number>(xAxis,yAxis);
    
        
        
        while(i<er.size()){
            List<PartyResults> lpr = er.get(i).getGlobalResults().getPartyResultsSorted();
            int j = 0;
            
            while(j < lpr.size()){
                XYChart.Series series = new XYChart.Series();
                series.setName(lpr.get(j).getParty());
                series.getData().add(new XYChart.Data(er.get(i).getYear()+"", lpr.get(j).getVotes()));
                lChart.getData().add(series);
                j++;
            }
                                                
            
            i++;
        }
    
    
        return lChart;
    }

    
    public BarChart barChart(List<ElectionResults>er){
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bChart = new BarChart<>(xAxis, yAxis);
        bChart.setTitle("Evolución de la distribución de votos del País Valencià ");
        XYChart.Series seriesCA = new XYChart.Series();
        XYChart.Series seriesVLC = new XYChart.Series();
        XYChart.Series seriesCTLN = new XYChart.Series();
        XYChart.Series seriesALCT = new XYChart.Series();
        seriesCA.setName("Comunidad Valenciana");
        seriesALCT.setName("Alicante");
        seriesCTLN.setName("Castellón");
        seriesVLC.setName("Valencia");
        int foo = 0;
        
        while (foo < er.size()) {
            RegionResults rg = er.get(foo).getGlobalResults();
            int year = er.get(foo).getYear();
            double votesCA = er.get(foo).getGlobalResults().getPollData().getVotes();
            double censusCA = er.get(foo).getGlobalResults().getPollData().getCensus();
            double percentageCA = votesCA / censusCA * 100;
            
            //Valencia
            double percentageVLC = (double) er.get(foo).getProvinceResults("Valencia").getPollData().getVotes() /
                    (double) er.get(foo).getProvinceResults("Valencia").getPollData().getCensus() * 100;
            double percentageALCT = (double) er.get(foo).getProvinceResults("Alicante").getPollData().getVotes() /
                    (double) er.get(foo).getProvinceResults("Alicante").getPollData().getCensus() * 100;
            double percentageCTLN = (double) er.get(foo).getProvinceResults("Castellón").getPollData().getVotes()
                    / (double) er.get(foo).getProvinceResults("Castellón").getPollData().getCensus() * 100;
            
            
            seriesCA.getData().remove(0, seriesCA.getData().size());
            seriesCA.getData().add(new XYChart.Data(year+"", percentageCA));                        
            seriesALCT.getData().add(new XYChart.Data(year+"",percentageALCT));            
            seriesCTLN.getData().add(new XYChart.Data(year + "", percentageCTLN));

            
            seriesVLC.getData().add(new XYChart.Data(year + "", percentageVLC));

                       
            
            foo++;
            
        }
        
        bChart.getData().addAll(seriesCA, seriesVLC, seriesALCT, seriesCTLN);
        bChart.setLegendVisible(true);
        return bChart;
    }
    

}
