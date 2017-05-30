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
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

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
    private Pane pane534;
    @FXML
    private ProgressBar progress;
    
      
    
    /// attrs
    int[] yearIndex  = {1995, 1999, 2003, 2007, 2011, 2015};
    List<ElectionResults> erList;
    @FXML
    private Label progressLabel;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // init autoField
        AutoCompleteTextField autoField = new AutoCompleteTextField();
        autoField.promptTextProperty().set("Año");                
        for(int i = 0; i<yearIndex.length;i++)
            autoField.getEntries().add(yearIndex[i]+"");        
        autoField.setMaxWidth(150);
        autoField.setPrefWidth(150);
        autoField.setDisable(true);
        formVbox.getChildren().add(0, autoField);
        
        // init several nodes
        pane2.setText("Distribución de votos");
        pane2.setText("Evolución de la participación");
        
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
                        
                        // converting year to list index 
                        int year = Integer.parseInt(autoField.getText());
                        int j = 0;
                        for (int i = 0; i < yearIndex.length; i++) {
                            if (year == yearIndex[i]) {
                                j = i;
                                break;
                            }
                        }
                        
                        // enable and populate - if needed - choiceboxes
                        provCB.setDisable(false);
                        regCB.setDisable(false);
                        if(provCB.getItems().isEmpty()){
                            provCB.setItems(FXCollections.observableArrayList(erList.get(j).getProvinces().keySet()));
                            regCB.setItems(FXCollections.observableArrayList(erList.get(j).getRegionProvinces().keySet()));
                        }
                        
                        // load charts that need the year input
                        loadYearData(erList, j);
                        
                        provCB.selectionModelProperty().addListener((event3)->{
                            int year2 = Integer.parseInt(autoField.getText());
                            int j1 = 0;
                            for (int i = 0; i < yearIndex.length; i++) {
                                if (year2 == yearIndex[i]) {
                                    j1 = i;
                                    break;
                                }
                            }
                            loadProvinceData(erList, provCB.getSelectionModel().getSelectedItem(), j1);
                            
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



    

    public void loadGeneralData(List<ElectionResults> er){
        hb3.getChildren().add(lineChart(er));
        hb2.getChildren().add(stackedBarChart(er));
        
    }    
    public void loadYearData(List<ElectionResults> er, int index){
        if(!hb1.getChildren().isEmpty())
            hb1.getChildren().remove(0, hb1.getChildren().size());
        
        if(!hb2.getChildren().isEmpty())
            hb2.getChildren().remove(1, hb2.getChildren().size());
        
        hb1.getChildren().add(0, caResults(er.get(index).getGlobalResults(), er.get(index).getYear()));
        hb2.getChildren().add(1, caBarResults(er.get(index).getGlobalResults(), er.get(index).getYear()));
    }
    
    public void loadProvinceData(List<ElectionResults> er, String prov, int index){
        hb1.getChildren().add(provinceResults(er.get(index).getProvinceResults(prov), prov));
        hb2.getChildren().add(provinceBarResults(er.get(index).getProvinceResults(prov), prov, index));        
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
    public BarChart provinceBarResults(RegionResults rg, String prov, int year){
        
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bChart = new BarChart<String, Number>(xAxis, yAxis);
        bChart.setTitle("Distribución de votos de " + prov);
                
        
        
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
    public BarChart regionBarResults(RegionResults rg, String reg, int year){
        
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bChart = new BarChart<String, Number>(xAxis, yAxis);
        bChart.setTitle("Distribución de votos de " + reg);
        
        
        
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
    public BarChart caBarResults(RegionResults rg, int year){
        
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bChart = new BarChart<String, Number>(xAxis, yAxis);
        bChart.setTitle("Distribución de votos del País Valencià en " + year);
        
        
        
        
        int i = 0;
        Collection<XYChart.Series> bcData = FXCollections.observableArrayList();
        while (i<rg.getPartyResultsSorted().size()){
            XYChart.Series series = new XYChart.Series();
            PartyResults pr= rg.getPartyResultsSorted().get(i);
            series.setName(pr.getParty());
            
            series.getData().add(new XYChart.Data(year+ "", pr.getVotes()));
            //displayLabelForData(new XYChart.Data(year+ "", pr.getVotes()));
            bChart.getData().add(series);                        
                        
            
            i++;
        }                                
        
        return bChart;
    }    
    private StackedBarChart stackedBarChart(List<ElectionResults> er){
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        StackedBarChart<String, Number> sbChart = new StackedBarChart<>(xAxis, yAxis);
        sbChart.setTitle("Evolución de la distribución de escaños del País Valencià ");
        
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
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lChart = new LineChart<>(xAxis, yAxis);
        lChart.setTitle("Evolución del voto en el País Valencià ");
        
        int foo = 0;
        while (foo < er.size()) {
            RegionResults rg = er.get(foo).getGlobalResults();

            int i = 0;
            //Collection<XYChart.Series> bcData = FXCollections.observableArrayList();
            while (i < rg.getPartyResultsSorted().size()) {
                XYChart.Series series = new XYChart.Series();
                PartyResults pr = rg.getPartyResultsSorted().get(i);
                series.setName(pr.getParty());
                series.getData().add(new XYChart.Data(er.get(foo).getYear() + "", pr.getVotes()));
                lChart.getData().add(series);

                i++;
            }

            foo++;
        }
        
        lChart.setLegendVisible(true);
        return lChart;
        
    }

    
    
}
