mdt task copy --in.src *KRCW-01ELIF027/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue --out.tar *내함_성형/Simulation/SimulationInfo.Inputs[0].InputValue

mdt task class mdt.test.task.TestMDTTaskModule1 --in.heater *KRCW-01ELIF027/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue --in.vaccum *KRCW-01ELQI005/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue --in.piercing *KRCW-01ESUM006/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue --in.inspection *KRCW-01ETHT006/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue --out.TotalThroughput *내함_성형/Simulation/SimulationInfo.Outputs[0].OutputValue

mdt task command "C:/Users/kwlee/AppData/Local/Programs/Microsoft VS Code/Code" --workingDir C:\Temp\test --in.heater *KRCW-01ELIF027/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue --in.vaccum *KRCW-01ELQI005/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue --in.piercing *KRCW-01ESUM006/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue --in.inspection *KRCW-01ETHT006/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue --out.TotalThroughput *내함_성형/Simulation/SimulationInfo.Outputs[0].OutputValue

remote http://localhost:12987/operations/test-operation --in.heater *KRCW-01ELIF027/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue --in.vaccum *KRCW-01ELQI005/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue --in.piercing *KRCW-01ESUM006/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue --in.inspection *KRCW-01ETHT006/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue --out.TotalThroughput *내함_성형/Simulation/SimulationInfo.Outputs[0].OutputValue



mdt get property 내함_성형/Simulation/SimulationInfo.Inputs[0] -r 1s
mdt get property 내함_성형/Simulation/SimulationInfo.Inputs[1] -r 1s
mdt get property 내함_성형/Simulation/SimulationInfo.Inputs[2] -r 1s
mdt get property 내함_성형/Simulation/SimulationInfo.Inputs[3] -r 1s
mdt get property 내함_성형/Simulation/SimulationInfo.Outputs[0] -r 1s



mdt task copy ^
--in.src *KRCW-01ETHT006/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--out.tar QC/Simulation/SimulationInfo.Inputs[3].InputValue

mdt task copy ^
--in.src *KRCW-01ESUM006/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--out.tar QC/Simulation/SimulationInfo.Inputs[2].InputValue

mdt task copy ^
--in.src *KRCW-01ELQI005/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--out.tar QC/Simulation/SimulationInfo.Inputs[1].InputValue

mdt task copy ^
--in.src *KRCW-01ELIF027/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--out.tar QC/Simulation/SimulationInfo.Inputs[0].InputValue



mdt task java ^
--class mdt.test.task.TestMDTTaskModule1 ^
--in.heater *KRCW-01ELIF027/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--in.vaccum *KRCW-01ELQI005/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--in.piercing *KRCW-01ESUM006/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--in.inspection *KRCW-01ETHT006/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--out.TotalThroughput *QC/Simulation/SimulationInfo.Outputs[0].OutputValue


mdt task program ^
--command "C:/Users/kwlee/AppData/Local/Programs/Microsoft VS Code/Code" ^
--workingDir C:\Temp\test ^
--in.heater *KRCW-01ELIF027/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--in.vaccum *KRCW-01ELQI005/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--in.piercing *KRCW-01ESUM006/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--in.inspection *KRCW-01ETHT006/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--out.TotalThroughput *QC/Simulation/SimulationInfo.Outputs[0].OutputValue


mdt task http ^
--url http://localhost:12987/operations/test-operation ^
--in.heater *KRCW-01ELIF027/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--in.vaccum *KRCW-01ELQI005/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--in.piercing *KRCW-01ESUM006/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--in.inspection *KRCW-01ETHT006/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--out.TotalThroughput *QC/Simulation/SimulationInfo.Outputs[0].OutputValue





mdt task set --value "10" --out.tar *QC/Simulation/SimulationInfo.Inputs[0].InputValue
mdt task set --value "20" --out.tar *QC/Simulation/SimulationInfo.Inputs[1].InputValue
mdt task set --value "30" --out.tar *QC/Simulation/SimulationInfo.Inputs[2].InputValue
mdt task set --value "40" --out.tar *QC/Simulation/SimulationInfo.Inputs[3].InputValue
mdt task set --value "" --out.tar *QC/Simulation/SimulationInfo.Outputs[0].OutputValue



mdt task program --command "C:/Users/kwlee/AppData/Local/Programs/Microsoft VS Code/Code" ^
--workingDir C:\Temp\test ^
--in.heater *QC/Simulation/SimulationInfo.Inputs[0].InputValue ^
--in.vaccum *QC/Simulation/SimulationInfo.Inputs[1].InputValue ^
--in.piercing QC/Simulation/SimulationInfo.Inputs[2].InputValue ^
--in.inspection QC/Simulation/SimulationInfo.Inputs[3].InputValue ^
--out.TotalThroughput *QC/Simulation/SimulationInfo.Outputs[0].OutputValue




mdt task set --value "unknown" --out.tar *QC/Simulation/SimulationInfo.Inputs[0].InputValue

mdt task copy ^
--in.src KRCW-01ELIF027/Data/DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue ^
--out.tar QC/Simulation/SimulationInfo.Inputs[0].InputValue




mdt task program ^
--command "C:/Users/kwlee/AppData/Local/Programs/Microsoft VS Code/Code" ^
--workingDir C:\Temp\test ^
--in.taktTimes *CRF/Simulation/SimulationInfo.Inputs[0].InputValue ^
--in.plans *CRF/Simulation/SimulationInfo.Inputs[1].InputValue ^
--in.products *CRF/Simulation/SimulationInfo.Inputs[2].InputValue ^
--in.resources *CRF/Simulation/SimulationInfo.Inputs[3].InputValue ^
--out.TotalThroughput *CRF/Simulation/SimulationInfo.Outputs[0].OutputValue
