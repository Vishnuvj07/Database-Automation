@Rule (name = "HandleTablespaceUtilization_checkParameters")
if(( HostLoginName notexists ) ||
( HostLoginName := "" ) ||
( DomainName notexists ) ||
( DomainName := "" ) ||
( DBLoginName notexists ) ||
( DBLoginName := "" ) ||
( HostName notexists ) ||
( HostName := "" ) ||
( DBInstanceName notexists ) ||
( DBInstanceName := "" ) ||
( TablespaceName notexists ) ||
( TablespaceName := "" ))
{
        (Log(Insufficient parameters or values))
        (Eject)
}
@Rule (name = "HandleTablespaceUtilization_SetOracleEnvVariable")
if(( HostLoginName !:= "" ) &&
( DomainName !:= "" ) &&
( DBLoginName !:= "" ) &&
( HostName !:= "" ) &&
( DBInstanceName !:= "" ) &&
( TablespaceName !:= "" ))
{

multiplyTwoNumbers(${DomainName},${HostLoginName},${HostName},"1024","30")

}
@Rule (name = "ManageTablespace_Error1multiplyTwoNumbers")
if (( multiplyTwoNumbers:Return != 0 ))
{
(Log(Failed to multiply datafile threshold and 1024 on the host ${HostName}))
        (Eject)
}        

@Rule (name = "ManageTablespace_divideTwoNumbers")
if (( multiplyTwoNumbers:Return = 0 ))
{

(SetOracleEnvVariableReturn1,SetOracleEnvVariableOutput1,OracleHomePath,ServiceName,PortNumber)=SetOracleEnvVariable(${DomainName},${HostLoginName},${HostName},${DBInstanceName})
}

@Rule (name = "HandleTablespaceUtilization_Error1SetOracleEnvVariable1")
if ((( SetOracleEnvVariableReturn1 != 0 ) && ( SetOracleEnvVariableOutput1 := "FailedToGetOHFrmOratabAndProcess" )) || (( SetOracleEnvVariableReturn1 != 0 ) && ( SetOracleEnvVariableOutput1 notexists )))
{        
          (Log(Failed to set oracle environment variable on the host ${HostName}))
        (Eject)
}  
@Rule (name = "HandleTablespaceUtilization_Error2SetOracleEnvVariable1")
if (( SetOracleEnvVariableReturn1 = 0 ) && ( SetOracleEnvVariableOutput1 !:= "FailedToGetOHFrmOratabAndProcess" ) && ( SetOracleEnvVariableOutput1 !:= "CreatedOraProfile" ))
{        
    (Log(Invalid unhandled output from child service operation SetOracleEnvVariable))
        (Eject)
} 
@Rule (name = "HandleTablespaceUtilization_getDBStatusWithoutSysdba")
if (( SetOracleEnvVariableReturn1 = 0 ) && ( SetOracleEnvVariableOutput1 := "CreatedOraProfile" ))
{  
  
  (getDBStatusReturn1,getDBStatusOutput1)=getDBStatusWithoutSysdba(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName})
}
@Rule (name = "HandleTablespaceUtilization_ErrorgetDBStatusWithoutSysdba")
if((( getDBStatusOutput1 := "NULL" ) && ( getDBStatusReturn1 = 0 )) || (( getDBStatusOutput1 := "" ) && ( getDBStatusReturn1 != 0 )))
{
        (Log(Failed to get the database status for DBInstance ${DBInstanceName} on the host ${HostName}))
        (Eject)
}


@Rule (name = "HandleTablespaceUtilization_getStringFromVariable")
if(( getDBStatusOutput1 !:= "OPEN" ) || ( getDBStatusReturn1 != 0 ))
{
        (getStringFromVariableReturn1,getStringFromVariableOutput1)=getStringFromVariable(${DomainName},${HostLoginName},${HostName},${getDBStatusOutput1},".*invalid username/password.*")
}

@Rule (name = "HandleTablespaceUtilization_ErrorgetStringFromVariable1")
if(( getStringFromVariableReturn1 = 0 ))
{
        (Log(Invalid DBLoginName or DBLoginPassword))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error1getStringFromVariable1")
if(( getStringFromVariableReturn1 != 0 ))
{
        (Log(The error is ${getDBStatusOutput1}))
        (Eject)
}
@Rule (name = "HandleTablespaceUtilization_getTablespace")
if(( getDBStatusOutput1 := "OPEN" ))  
{
(getTablespaceReturn1,getTablespaceOutput1)=getTablespace(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},${TablespaceName})
}

@Rule (name = "HandleTablespaceUtilization_Error1getTablespace")
if(( getTablespaceReturn1 != 0 ))
{
        (Log(Failed to execute the block to get tablespace name on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error2getTablespace")
if(( getTablespaceOutput1 := "NULL" ))
{
        (Log(Failed to get tablespace name in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_getTableSpaceFreeSpaceAvailabilityThreshold")
if(( getTablespaceOutput1 !:= "NULL" ))
{

(getTablespaceSizeWithThresholdReturn1,getTablespaceSizeWithThresholdOutput1) = getTablespaceSizeWithThreshold(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}))
}

@Rule (name = "HandleTablespaceUtilization_Error1getTableSpaceFreeSpaceAvailabilityThreshold")
if(( getTablespaceSizeWithThresholdReturn1 != 0 ))
{
        (Log(Failed to execute the block to get tablespace resize size on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error2getTableSpaceFreeSpaceAvailabilityThreshold")
if(( getTablespaceSizeWithThresholdOutput1 := "NULL" ))
{
        (Log(Failed to get tablespace ${TablespaceName} resize size in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}


@Rule (name = "ManageTablespace_checkDFThresholdMax")
if (( getTablespaceSizeWithThresholdOutput1 > multiplyTwoNumbers:Output  ))
{

        (Log(Failed to get tablespace ${TablespaceName} resize size in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
  
}
@Rule (name = "HandleTablespaceUtilization_getTempTablespace")
if(( getTablespaceOutput1 !:= "NULL" ) && ( getTablespaceSizeWithThresholdOutput1 <= multiplyTwoNumbers:Output  ))
{

(getTempTablespaceReturn1,getTempTablespaceOutput1) = getTempTablespace(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}))
}

@Rule (name = "HandleTablespaceUtilization_Error1getTempTablespace")
if(( getTempTablespaceReturn1 != 0 ))
{
        (Log(Failed to execute the block to get tablespace resize size on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error2getTempTablespace")
if(( getTempTablespaceOutput1 !:= "NULL" ))
{
        (Log(The tablespace ${TablespaceName} is temp tablespace in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}


@Rule (name = "HandleTablespaceUtilization_getUndoTablespace")
if(( getTempTablespaceOutput1 := "NULL" ))
{
(getUndoTablespaceReturn1,getUndoTablespaceOutput1) = getUndoTablespace(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}))
}

@Rule (name = "HandleTablespaceUtilization_Error1getUndoTablespace")
if(( getUndoTablespaceReturn1 != 0 ))
{
        (Log(Failed to execute the block to get tablespace resize size on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error2getUndoTablespace")
if(( getUndoTablespaceOutput1 !:= "NULL" ))
{
        (Log(The tablespace ${TablespaceName} is undo tablespace in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}


@Rule (name = "HandleTablespaceUtilization_getTablespaceNameCountMaxis")
if(( getUndoTablespaceOutput1 := "NULL" ))
{


(getTablespaceNameCountMaxisReturn1,getTablespaceNameCountMaxisOutput1) = getTablespaceNameCountMaxis(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}))
}

@Rule (name = "HandleTablespaceUtilization_Error1getTablespaceNameCountMaxis")
if(( getTablespaceNameCountMaxisReturn1 != 0 ))
{
        (Log(Failed to execute the block to get tablespace resize size on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error2getTablespaceNameCountMaxis")
if(( getTablespaceNameCountMaxisOutput1 !:= "NULL" ))
{
        (Log(The tablespace ${TablespaceName} is having more than 900 in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}


@Rule (name = "HandleTablespaceUtilization_getDatafileThresholdMaxis")
if(( getTablespaceNameCountMaxisOutput1 := "NULL" ))
{
(getDatafileThresholdMaxisReturn1,getDatafileThresholdMaxisOutput1) = getDatafileThresholdMaxis(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName})
}

@Rule (name = "HandleTablespaceUtilization_Error1getDatafileThresholdMaxis")
if(( getDatafileThresholdMaxisReturn1 != 0 ))
{
        (Log(Failed to execute the block to get tablespace resize size on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error2getDatafileThresholdMaxis")
if(( getDatafileThresholdMaxisOutput1 := "NULL" ))
{
        (Log(The tablespace ${TablespaceName} is temp tablespace in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}
@Rule (name = "HandleTablespaceUtilization_Error3getDatafileThresholdMaxis")
if(( getDatafileThresholdMaxisOutput1 !:= "NULL" ) && ( getDatafileThresholdMaxisOutput1 > 90 ))
{
        (Log(The tablespace ${TablespaceName} is having more than 90% of db files in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}


@Rule (name = "HandleTablespaceUtilization_getTableSpaceFreeSpaceAvailabilityInMB")
if(( getDatafileThresholdMaxisOutput1 !:= "NULL" ) && ( getDatafileThresholdMaxisOutput1 < 90 ))
{

(getTableSpaceFreeSpaceAvailabilityInMBReturn1,getTableSpaceFreeSpaceAvailabilityInMBOutput1) = getTableSpaceFreeSpaceAvailabilityInMB(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}))
}

@Rule (name = "HandleTablespaceUtilization_Error1getTableSpaceFreeSpaceAvailabilityInMB")
if(( getTableSpaceFreeSpaceAvailabilityInMBReturn1 != 0 ))
{
        (Log(Failed to execute the block to get tablespace free space size on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error2getTableSpaceFreeSpaceAvailabilityInMB")
if(( getTableSpaceFreeSpaceAvailabilityInMBOutput1 := "NULL" ))
{
        (Log(Failed to get tablespace ${TablespaceName} free space size in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_getTableSpaceFreeSpaceAvailabilityInPercent")
if(( getTableSpaceFreeSpaceAvailabilityInMBOutput1 !:= "NULL" ))
{
(getTableSpaceFreeSpaceAvailabilityInPercentReturn1,getTableSpaceFreeSpaceAvailabilityInPercentOutput1) = getTableSpaceFreeSpaceAvailabilityInPercent(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}))
}

@Rule (name = "HandleTablespaceUtilization_Error1getTableSpaceFreeSpaceAvailabilityInPercent")
if(( getTableSpaceFreeSpaceAvailabilityInPercentReturn1 != 0 ))
{
        (Log(Failed to execute the block to convert decimal to integer on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error2getTableSpaceFreeSpaceAvailabilityInPercent")
if(( getTableSpaceFreeSpaceAvailabilityInPercentOutput1 := "NULL" ))
{
        (Log(Failed to convert decimal ${getTSUsedSizeUsingPercentageOutput1} to integer in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_getTablespaceUsedPercent")
if(( getTableSpaceFreeSpaceAvailabilityInPercentOutput1 !:= "NULL" ))
{
(getTablespaceUsedPercentReturn1,getTablespaceUsedPercentOutput1) = getTablespaceUsedPercent(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}))
}

@Rule (name = "HandleTablespaceUtilization_Error1getTablespaceUsedPercent")
if(( getTablespaceUsedPercentReturn1 != 0 ))
{
        (Log(Failed to execute the block to convert decimal to integer on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error2getTablespaceUsedPercent")
if(( getTablespaceUsedPercentOutput1 := "NULL" ))
{
        (Log(Failed to convert decimal ${getTablespaceUsedPercentOutput1} to integer in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_compareValues")
if(( getTablespaceUsedPercentOutput1 !:= "NULL" ))
{

        (CompareReturn,CompareOutput)=compareValues(${DomainName},${HostLoginName},${HostName},"80",${getTablespaceUsedPercentOutput1})

}

@Rule (name = "CompareValues_ErrorCompareValues")
if(( CompareReturn != 0 ))
{
        (Log(Failed to compare Threshold values on host ${HostName}))
        (Eject)
}
@Rule (name = "CompareValues_Error1Value2IsLessThanValue1")
if(( CompareReturn = 0 ) && ( CompareOutput = 0 ))
{
        (Log(Tablespace used percentage is lesser than Threshold value on host ${HostName}))
        (Eject)
}

@Rule (name = "CompareValues_ForValue2IsGreaterOrEqualToValue1")
if(( CompareReturn = 0 ) && ( CompareOutput = 1 ))
{

(getTablespaceSizeInMBReturn1,getTablespaceSizeInMBOutput1) = getTablespaceSizeInMB(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}))

}

@Rule (name = "HandleTablespaceUtilization_Error1getTablespaceSizeInMB")
if(( getTablespaceSizeInMBReturn1 != 0 ))
{
        (Log(Failed to execute the block to convert decimal to integer on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error2getTablespaceSizeInMB")
if(( getTablespaceSizeInMBOutput1 := "NULL" ))
{
        (Log(Failed to convert decimal ${getTSUsedSizeUsingPercentageOutput1} to integer in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_getDataFilePath")
if(( getTablespaceSizeInMBOutput1 !:= "NULL" ))
{

        (getDataFilePathReturn1,getDataFilePathOutput1)=getDataFilePath(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}))

}


 @Rule (name = "ManageTablespace_Error1getDataFilePath")
if (( getDataFilePathReturn1 != 0 ))
{
(Log(Failed to execute the block to get existing datafile with autoextend values on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error2getDataFilePath")
if(( getDataFilePathOutput1 := "NULL" ))
{
        (Log(Failed to convert decimal ${getTSUsedSizeUsingPercentageOutput1} to integer in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}

 @Rule (name = "ManageTablespace_getDataFileWithAutoExtend")
if (( getDataFilePathOutput1 !:= "NULL" ))

{
 getDataFileWithAutoExtend(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}),${multiplyTwoNumbers:Output})

} 

 @Rule (name = "ManageTablespace_Error1getDataFileWithAutoExtend")
if (( getDataFileWithAutoExtend:Return != 0 ))
{
(Log(Failed to execute the block to get existing datafile with autoextend values on the host ${HostName}))
        (Eject)
}        
        
@Rule (name = "ManageTablespace_Error2getDataFileWithAutoExtend")
if ( ( getDataFileWithAutoExtend:Output := "FAILED" ))
{
(Log(Failed to get datafile with autoextend values in the database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}        
@Rule (name = "ManageTablespace_getTabDataFileWithSize")
if ((( getDataFileWithAutoExtend:Output !:= "NULL" ) && ( getDataFileWithAutoExtend:Output !:= "FAILED" )) || ( getDataFileWithAutoExtend:Output := "NULL" ))  
{

(getTabDataFileWithSizeReturn1,getTabDataFileWithSizeOutput1) = getTabDataFileSizeinMB(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}),${multiplyTwoNumbers:Output})

}

@Rule (name = "HandleTablespaceUtilization_Error1getTabDataFileWithSize")
if(( getTabDataFileWithSizeReturn1 != 0 ))
{
        (Log(Failed to execute the block to convert decimal to integer on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error2getTabDataFileWithSize")
if(( getTabDataFileWithSizeOutput1 := "NULL" ))
{
        (Log(Failed to convert decimal ${getTSUsedSizeUsingPercentageOutput1} to integer in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_convertDecimalToInteger")
if(( getTabDataFileWithSizeOutput1 !:= "NULL" ))
{

(convertDecimalToIntegerReturn1,convertDecimalToIntegerOutput1)=convertDecimalToInteger(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},${getTablespaceUsedPercentOutput1})
}

@Rule (name = "HandleTablespaceUtilization_Error1convertDecimalToInteger")
if(( convertDecimalToIntegerReturn1 != 0 ))
{
        (Log(Failed to execute the block to convert decimal to integer on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error2convertDecimalToInteger")
if(( convertDecimalToIntegerOutput1 := "NULL" ))
{
        (Log(Failed to convert decimal ${getTSUsedSizeUsingPercentageOutput1} to integer in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_resizeExistingDatafileStmt")
if(( convertDecimalToIntegerOutput1 !:= "NULL" ))
{

(resizeExistingDatafileStmtReturn1,resizeExistingDatafileStmtOutput1)=resizeExistingDatafileMax(${DomainName},${HostLoginName},${HostName},"fn:toUpperCase(${getTablespaceOutput1}):${getTablespaceSizeWithThresholdOutput1}",${getTabDataFileWithSizeOutput1},"30",${getDataFilePathOutput1},"80")

}


@Rule (name = "ManageTablespace_Error1resizeExistingDatafileStmt")
if (( resizeExistingDatafileStmtReturn1 != 0 ))
{
(Log(Failed to generate resize existing datafile statement on the host ${HostName}))
        (Eject)
}
@Rule (name = "ManageTablespace_getStringFromVariable")
if (( resizeExistingDatafileStmtReturn1 = 0 ))
{
        (getStringFromVariableReturn1,getStringFromVariableOutput1)=getStringFromVariable(${DomainName},${HostLoginName},${HostName},${resizeExistingDatafileStmtOutput1},"Insufficient")
}


 


 @Rule (name = "ManageTablespace_splitStringByIndex1")
if((getStringFromVariableReturn1 != 0 ))
{
  
  (splitStringByIndexReturn1,splitStringByIndexOutput1Count) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${resizeExistingDatafileStmtOutput1},"=","1")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex1Error")
if((splitStringByIndexReturn1 != 0 ))
{

        (Log(Unable to split Datafile count))
        (Eject)

}


 @Rule (name = "ManageTablespace_splitStringByIndex2")
if((splitStringByIndexReturn1 = 0 ) && (resizeExistingDatafileStmtOutput1 !:= "") )
{
  
  (splitStringByIndexReturn2,splitStringByIndexOutput1add) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${resizeExistingDatafileStmtOutput1},"=","3")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex2Error")
if((splitStringByIndexReturn2 != 0 ))
{

        (Log(Unable to split Datafile size))
        (Eject)

}

 @Rule (name = "ManageTablespace_splitStringByIndex3")
if((splitStringByIndexReturn2 = 0 ) && (splitStringByIndexOutput1add !:= "") )
{
  
  (splitStringByIndexReturn3,splitStringByIndexOutput1addsize) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput1add},":","2")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex3Error")
if((splitStringByIndexReturn3 != 0 ))
{

        (Log(Unable to split add Datafile size))
        (Eject)

}


 @Rule (name = "ManageTablespace_splitStringByIndex4")
if((splitStringByIndexReturn1 = 0 ) && (resizeExistingDatafileStmtOutput1 !:= "") && (splitStringByIndexOutput1Count := "1"))
{

  (splitStringByIndexReturn4,splitStringByIndexOutput1DFstmt) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${resizeExistingDatafileStmtOutput1},"=","2")

}

 @Rule (name = "ManageTablespace_splitStringByIndex4Error")
if((splitStringByIndexReturn4 != 0 ))
{

        (Log(Unable to split Datafiles))
        (Eject)

}

 @Rule (name = "ManageTablespace_splitStringByIndex5")
if((splitStringByIndexReturn4 = 0 ) && (splitStringByIndexOutput1DFstmt !:= ""))
{

  
  (splitStringByIndexReturn5,splitStringByIndexOutput1DF1) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput1DFstmt},",","1")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex5Error")
if((splitStringByIndexReturn5 != 0 ))
{

        (Log(Unable to split 1st Datafile ))
        (Eject)

}



 @Rule (name = "ManageTablespace_splitStringByIndex6")
if((splitStringByIndexReturn5 = 0 ) && (splitStringByIndexOutput1DF1 !:= ""))
{

  
  (splitStringByIndexReturn6,splitStringByIndexOutput1DFName) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput1DF1},":","1")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex6Error")
if((splitStringByIndexReturn6 != 0 ))
{

        (Log(Unable to split 1st Datafile name))
        (Eject)

}


 @Rule (name = "ManageTablespace_splitStringByIndex7")
if((splitStringByIndexReturn6 = 0 ) && (splitStringByIndexOutput1DF1 !:= ""))
{
  
  (splitStringByIndexReturn7,splitStringByIndexOutput1DFSize) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput1DF1},":","2")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex7Error")
if((splitStringByIndexReturn7 != 0 ))
{

        (Log(Unable to split 1st Datafile size))
        (Eject)

}



@Rule (name = "ManageTablespace_splitStringByIndex8")
if((splitStringByIndexOutput1DFstmt !:= "") && (splitStringByIndexOutput1Count := "2"))
{

  
  (splitStringByIndexReturn8,splitStringByIndexOutput1DF2) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput1DFstmt},",","2")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex8Error")
if((splitStringByIndexReturn8 != 0 ))
{

        (Log(Unable to split 2nd Datafile ))
        (Eject)

}



 @Rule (name = "ManageTablespace_splitStringByIndex9")
if((splitStringByIndexReturn8 = 0 ) && (splitStringByIndexOutput1DF2 !:= ""))
{

  
  (splitStringByIndexReturn9,splitStringByIndexOutput1DFName2) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput1DF2},":","1")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex9Error")
if((splitStringByIndexReturn9 != 0 ))
{

        (Log(Unable to split 2nd Datafile name))
        (Eject)

}


 @Rule (name = "ManageTablespace_splitStringByIndex10")
if((splitStringByIndexReturn1 = 0 ) && (splitStringByIndexOutput1DF2 !:= ""))
{
  
  (splitStringByIndexReturn10,splitStringByIndexOutput1DFSize2) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput1DF2},":","2")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex10Error")
if((splitStringByIndexReturn10 != 0 ))
{

        (Log(Unable to split 2nd Datafile size))
        (Eject)

}



@Rule (name = "ManageTablespace_splitStringByIndex11")
if((splitStringByIndexOutput1DFstmt !:= "") && (splitStringByIndexOutput1Count := "3"))
{

  
  (splitStringByIndexReturn11,splitStringByIndexOutput1DF3) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput1DFstmt},",","3")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex11Error")
if((splitStringByIndexReturn11 != 0 ))
{

        (Log(Unable to split 3rd Datafile ))
        (Eject)

}



 @Rule (name = "ManageTablespace_splitStringByIndex12")
if((splitStringByIndexReturn11 = 0 ) && (splitStringByIndexOutput1DF3 !:= ""))
{

  
  (splitStringByIndexReturn13,splitStringByIndexOutput1DFName3) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput1DF3},":","1")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex12Error")
if((splitStringByIndexReturn13 != 0 ))
{

        (Log(Unable to split 3rd Datafile name))
        (Eject)

}


 @Rule (name = "ManageTablespace_splitStringByIndex13")
if((splitStringByIndexReturn13 = 0 ) && (splitStringByIndexOutput1DF3 !:= ""))
{
  
  (splitStringByIndexReturn14,splitStringByIndexOutput1DFSize3) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput1DF3},":","2")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex13Error")
if((splitStringByIndexReturn14 != 0 ))
{

        (Log(Unable to split 3rd Datafile size))
        (Eject)

}

 @Rule (name = "ManageTablespace_ResizeDatafile1")
if((splitStringByIndexOutput1DFName !:= "") && (splitStringByIndexOutput1DFSize !:= "") && (splitStringByIndexOutput1Count := "1"))
{
(resizeDataFileReturn1,resizeDataFileOutput1)=resizeDataFile(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},${splitStringByIndexOutput1DFName},${splitStringByIndexOutput1DFSize})
}

 @Rule (name = "ManageTablespace_ResizeDatafile2")
if((splitStringByIndexOutput1DFName2 !:= "") && (splitStringByIndexOutput1DFSize2 !:= "") && (splitStringByIndexOutput1Count := "2"))
{
(resizeDataFileReturn2,resizeDataFileOutput2)=resizeDataFile(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},${splitStringByIndexOutput1DFName},${splitStringByIndexOutput1DFSize})
}

 @Rule (name = "ManageTablespace_ResizeDatafile3")
if((splitStringByIndexOutput1DFName3 !:= "") && (splitStringByIndexOutput1DFSize3 !:= "") && (splitStringByIndexOutput1Count := "3"))
{
(resizeDataFileReturn3,resizeDataFileOutput3)=resizeDataFile(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},${splitStringByIndexOutput1DFName},${splitStringByIndexOutput1DFSize})
}


 @Rule (name = "ManageTablespace_ResizeDatafileError1")
if((resizeDataFileReturn1 != 0 ) || (resizeDataFileReturn2 != 0 ) || (resizeDataFileReturn3 != 0 ))
{

        (Log(Failed to execute the block to Resize the Datafile ))
        (Eject)

}

 @Rule (name = "ManageTablespace_ResizeDatafileError2")
if((resizeDataFileOutput1 := "NULL" ) || (resizeDataFileOutput2 := "NULL" ) || (resizeDataFileOutput3 := "NULL" ))
{

        (Log(Unable to Resize the Datafile ))
        (Eject)

}



 @Rule (name = "ManageTablespace_ResizeDatafileSuccess")
if((resizeDataFileOutput1 := "Success" ) || (resizeDataFileOutput2 := "Success" ) || (resizeDataFileOutput3 := "Success" ))
{

(getTablespaceUsedPercentReturn21,getTablespaceUsedPercentOutput21) = getTablespaceUsedPercent(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}))

}

@Rule (name = "HandleTablespaceUtilization_Error1getTablespaceUsedPercent21")
if(( getTablespaceUsedPercentReturn21 != 0 ))
{
        (Log(Failed to execute the block to convert decimal to integer on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error2getTablespaceUsedPercent21")
if(( getTablespaceUsedPercentOutput21 := "NULL" ))
{
        (Log(Failed to convert decimal ${getTSUsedSizeUsingPercentageOutput1} to integer in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_compareValues21")
if(( getTablespaceUsedPercentOutput21 !:= "NULL" ))
{

        (CompareReturn21,CompareOutput21)=compareValues(${DomainName},${HostLoginName},${HostName},"84",${getTablespaceUsedPercentOutput21})

}

@Rule (name = "CompareValues_ErrorCompareValues21")
if(( CompareReturn21 != 0 ))
{
        (Log(Failed to compare Threshold values on host ${HostName}))
        (Eject)
}


@Rule (name = "CompareValues_ResolvedForValue2IsGreaterOrEqualToValue11")
if((( CompareReturn21 = 0 ) && ( CompareOutput21 = 0 )) && ((splitStringByIndexOutput1Count = 1) && (resizeDataFileOutput1 := "Success" ) && (splitStringByIndexOutput1addsize <= 0)))
{

        (sendEmailReturn1,sendEmailOutput1)=IgnioSystemLib:SendMail(${MailID},"","","","The Existing Datafile ${splitStringByIndexOutput1DFName} is Resized to ${splitStringByIndexOutput1DFSize} and the Current Tablespace threshold value is ${getTablespaceUsedPercentOutput21} ","Tablespace Utilization Status of ${TablespaceName} in database ${DBInstanceName} and Incident Number is : ${IncidentNumber}")
}

@Rule (name = "CompareValues_ResolvedForValue2IsGreaterOrEqualToValue12")
if ((( CompareReturn21 = 0 ) && ( CompareOutput21 = 0 )) && ((splitStringByIndexOutput1Count = 2) && (resizeDataFileOutput1 := "Success" ) && (resizeDataFileOutput2 := "Success" ) && (splitStringByIndexOutput1addsize <= 0)))
{
        (sendEmailReturn1,sendEmailOutput1)=IgnioSystemLib:SendMail(${MailID},"","","","The Existing Datafiles ${splitStringByIndexOutput1DFName} is Resized to ${splitStringByIndexOutput1DFSize}, ${splitStringByIndexOutput2DFName} is Resized to ${splitStringByIndexOutput2DFSize} and the Current Tablespace threshold value is ${getTablespaceUsedPercentOutput21} ","Tablespace Utilization Status of ${TablespaceName} in database ${DBInstanceName} and Incident Number is : ${IncidentNumber}")
}

@Rule (name = "CompareValues_ResolvedForValue2IsGreaterOrEqualToValue13")
if ((( CompareReturn21 = 0 ) && ( CompareOutput21 = 0 )) && ((splitStringByIndexOutput1Count = 3) && (resizeDataFileOutput1 := "Success" ) && (resizeDataFileOutput2 := "Success" ) && (resizeDataFileOutput3 := "Success" ) && (splitStringByIndexOutput1addsize <= 0)))
{
        (sendEmailReturn1,sendEmailOutput1)=IgnioSystemLib:SendMail(${MailID},"","","","The Existing Datafiles ${splitStringByIndexOutput1DFName} is Resized to ${splitStringByIndexOutput1DFSize}, ${splitStringByIndexOutput2DFName} is Resized to ${splitStringByIndexOutput2DFSize}, ${splitStringByIndexOutput3DFName} is Resized to ${splitStringByIndexOutput3DFSize} and the Current Tablespace threshold value is ${getTablespaceUsedPercentOutput21} ","Tablespace Utilization Status of ${TablespaceName} in database ${DBInstanceName} and Incident Number is : ${IncidentNumber}")
}


@Rule (name = "CompareValues_ResolvedForValue2sendMail1")
if(( sendEmailReturn1 = 0 ))
{
        
        
          (Log(Tablespace ${TablespaceName} resized successfully in the database ${DBInstanceName} on the host ${HostName} and mail has been sent to the mail id ${MailID}))
        (Resolved("Success"))

}

@Rule (name = "CompareValues_ErrorsendMail1")
if(( sendEmailReturn1 != 0 ))
{
        (Log(Failed to send mail))
        (Eject)
}


@Rule (name = "ManageTablespace_ErrorAltercommandMax")
if((splitStringByIndexOutput1addsize > 0 ) && (splitStringByIndexOutput1Count = 3))
  {
     (Log(Not able to reduce the threshold under 84%, Alter command reached max level))
     (Eject)
    
  }



@Rule (name = "ManageTablespace_ErrorresizeDataFile2")
if((splitStringByIndexOutput1addsize > 0 ) )
{
(getAllDataFilePathReturn1,getAllDataFilePathOutput1)=getAllDataFilePath(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName})
}

@Rule (name = "ManageTablespace_Error1getAllDataFilePath")
if (( getAllDataFilePathReturn1 != 0 ))
{
(Log(Failed to execute the block to get all datafile path on the host ${HostName}))
        (Eject)
}        
        
@Rule (name = "ManageTablespace_Error2getAllDataFilePath")
if ( ( getAllDataFilePathOutput1 := "NULL" ))
{
(Log(Datafile path does not exist in the database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}        
@Rule (name = "ManageTablespace_getUniqueValues")
if (( getAllDataFilePathOutput1 !:= "NULL" ))
{        
        
        
(addDatafileStmtReturn1,addDatafileStmtOutput1)=addDataFileMax(${DomainName},${HostLoginName},${HostName},"fn:toUpperCase(${getTablespaceOutput1}):${splitStringByIndexOutput1addsize}",${getDataFilePathOutput1},"80","30",${getAllDataFilePathOutput1})
        
}

@Rule (name = "ManageTablespace_Error1addDatafileStmt")
if (( addDatafileStmtReturn1 != 0 ))
{
(Log(Failed to generate resize existing datafile statement on the host ${HostName}))
        (Eject)
}
@Rule (name = "ManageTablespace_getStringFromVariable2")
if (( addDatafileStmtReturn1 = 0 ) && (addDatafileStmtOutput1 !:= ""))
{
        (getStringFromVariableReturn2,getStringFromVariableOutput2)=getStringFromVariable(${DomainName},${HostLoginName},${HostName},${addDatafileStmtOutput1},"Error")
}



 @Rule (name = "ManageTablespace_splitStringByIndex1Count")
if((getStringFromVariableReturn2 != 0 ))
{
  
  (splitStringByIndexReturnc2,splitStringByIndexOutput2Count) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${addDatafileStmtOutput1},"=","1")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex1CountError")
if((splitStringByIndexReturnc2 != 0 ))
{

        (Log(Unable to split Datafile count))
        (Eject)

}


@Rule (name = "ManageTablespace_splitStringByIndexAddsize2")
if((splitStringByIndexReturnc2 = 0 ) && (addDatafileStmtOutput1 !:= "") )
{
  
  (splitStringByIndexReturnAdd2,splitStringByIndexOutput2add) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${addDatafileStmtOutput1},"=","3")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex2AddsizeError")
if((splitStringByIndexReturnAdd2 != 0 ))
{

        (Log(Unable to split Datafile size))
        (Eject)

}

 @Rule (name = "ManageTablespace_splitStringByIndex3Add")
if((splitStringByIndexReturnAdd2 = 0 ) && (splitStringByIndexOutput2add !:= "") )
{
  
  (splitStringByIndexReturnAdd3,splitStringByIndexOutput2addsize) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput2add},":","2")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex3AddError")
if((splitStringByIndexReturnAdd3 != 0 ))
{

        (Log(Unable to split add Datafile size))
        (Eject)

}

 @Rule (name = "ManageTablespace_splitStringByIndex41")
if((splitStringByIndexReturnAdd3 = 0 ) && (addDatafileStmtOutput1 !:= "") && (splitStringByIndexOutput2Count := "1"))
{

  (splitStringByIndexReturn41,splitStringByIndexOutput41DFstmt) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${addDatafileStmtOutput1},"=","2")

}

 @Rule (name = "ManageTablespace_splitStringByIndex41Error")
if((splitStringByIndexReturn41 != 0 ))
{

        (Log(Unable to split Datafiles))
        (Eject)

}

 @Rule (name = "ManageTablespace_splitStringByIndex51")
if((splitStringByIndexReturn41 = 0 ) && (splitStringByIndexOutput41DFstmt !:= ""))
{

  
  (splitStringByIndexReturn51,splitStringByIndexOutput51DF1) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput41DFstmt},",","1")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex51Error")
if((splitStringByIndexReturn51 != 0 ))
{

        (Log(Unable to split 1st Datafile ))
        (Eject)

}



 @Rule (name = "ManageTablespace_splitStringByIndex61")
if((splitStringByIndexReturn51 = 0 ) && (splitStringByIndexOutput51DF1 !:= ""))
{

  
  (splitStringByIndexReturn61,splitStringByIndexOutput61DFName) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput51DF1},":","1")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex61Error")
if((splitStringByIndexReturn61 != 0 ))
{

        (Log(Unable to split 1st Datafile name))
        (Eject)

}


 @Rule (name = "ManageTablespace_splitStringByIndex71")
if((splitStringByIndexReturn61 = 0 ) && (splitStringByIndexOutput51DF1 !:= ""))
{
  
  (splitStringByIndexReturn71,splitStringByIndexOutput71DFSize) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput51DF1},":","2")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex71Error")
if((splitStringByIndexReturn71 != 0 ))
{

        (Log(Unable to split 1st Datafile size))
        (Eject)

}















 @Rule (name = "ManageTablespace_splitStringByIndex412C")
if((splitStringByIndexReturnAdd3 = 0 ) && (addDatafileStmtOutput1 !:= "") && (splitStringByIndexOutput2Count := "2"))
{

  (splitStringByIndexReturn412,splitStringByIndexOutput412DFstmt) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${addDatafileStmtOutput1},"=","2")

}

 @Rule (name = "ManageTablespace_splitStringByIndex41Error2C")
if((splitStringByIndexReturn412 != 0 ))
{

        (Log(Unable to split Datafiles))
        (Eject)

}

 @Rule (name = "ManageTablespace_splitStringByIndex512C")
if((splitStringByIndexReturn412 = 0 ) && (splitStringByIndexOutput412DFstmt !:= ""))
{

  
  (splitStringByIndexReturn512,splitStringByIndexOutput512DF1) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput412DFstmt},",","1")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex51Error2C")
if((splitStringByIndexReturn512 != 0 ))
{

        (Log(Unable to split 1st Datafile ))
        (Eject)

}



 @Rule (name = "ManageTablespace_splitStringByIndex612C")
if((splitStringByIndexReturn512 = 0 ) && (splitStringByIndexOutput512DF1 !:= ""))
{

  
  (splitStringByIndexReturn612,splitStringByIndexOutput612DFName) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput512DF1},":","1")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex61Error2C")
if((splitStringByIndexReturn612 != 0 ))
{

        (Log(Unable to split 1st Datafile name))
        (Eject)

}


 @Rule (name = "ManageTablespace_splitStringByIndex712C")
if((splitStringByIndexReturn612 = 0 ) && (splitStringByIndexOutput51DF1 !:= ""))
{
  
  (splitStringByIndexReturn712,splitStringByIndexOutput712DFSize) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput512DF1},":","2")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex71Error2C")
if((splitStringByIndexReturn712 != 0 ))
{

        (Log(Unable to split 1st Datafile size))
        (Eject)

}




 @Rule (name = "ManageTablespace_splitStringByIndex512C")
if((splitStringByIndexReturn41 = 0 ) && (splitStringByIndexOutput41DFstmt !:= ""))
{

  
  (splitStringByIndexReturn51B,splitStringByIndexOutput51BDF1) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput41DFstmt},",","2")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex51Error2C")
if((splitStringByIndexReturn51B != 0 ))
{

        (Log(Unable to split 2nd Datafile ))
        (Eject)

}



 @Rule (name = "ManageTablespace_splitStringByIndex612C")
if((splitStringByIndexReturn51B = 0 ) && (splitStringByIndexOutput51BDF1 !:= ""))
{

  
  (splitStringByIndexReturn61B,splitStringByIndexOutput61BDFName) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput51BDF1},":","1")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex61Error2C")
if((splitStringByIndexReturn61B != 0 ))
{

        (Log(Unable to split 2nd Datafile name))
        (Eject)

}


 @Rule (name = "ManageTablespace_splitStringByIndex712C")
if((splitStringByIndexReturn61B = 0 ) && (splitStringByIndexOutput51BDF1 !:= ""))
{
  
  (splitStringByIndexReturn71B,splitStringByIndexOutput71BDFSize) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput51DF1},":","2")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex71Error2C")
if((splitStringByIndexReturn71B != 0 ))
{

        (Log(Unable to split 2nd Datafile size))
        (Eject)

}




 @Rule (name = "ManageTablespace_splitStringByIndex413C")
if((splitStringByIndexReturnAdd3 = 0 ) && (addDatafileStmtOutput1 !:= "") && (splitStringByIndexOutput2Count := "3"))
{

  (splitStringByIndexReturn41C,splitStringByIndexOutput41CDFstmt) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${addDatafileStmtOutput1},"=","2")

}

 @Rule (name = "ManageTablespace_splitStringByIndex41Error3C")
if((splitStringByIndexReturn41C != 0 ))
{

        (Log(Unable to split Datafiles))
        (Eject)

}

 @Rule (name = "ManageTablespace_splitStringByIndex513C")
if((splitStringByIndexReturn41C = 0 ) && (splitStringByIndexOutput41CDFstmt !:= ""))
{

  
  (splitStringByIndexReturn51C,splitStringByIndexOutput51CDF1) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput41CDFstmt},",","1")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex51Error3C")
if((splitStringByIndexReturn51C != 0 ))
{

        (Log(Unable to split 1st Datafile ))
        (Eject)

}



 @Rule (name = "ManageTablespace_splitStringByIndex613C")
if((splitStringByIndexReturn51C = 0 ) && (splitStringByIndexOutput51CDF1 !:= ""))
{

  
  (splitStringByIndexReturn61C,splitStringByIndexOutput61CDFName) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput51CDF1},":","1")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex61Error3C")
if((splitStringByIndexReturn61C != 0 ))
{

        (Log(Unable to split 1st Datafile name))
        (Eject)

}


 @Rule (name = "ManageTablespace_splitStringByIndex713C")
if((splitStringByIndexReturn61C = 0 ) && (splitStringByIndexOutput51CDF1 !:= ""))
{
  
  (splitStringByIndexReturn71C,splitStringByIndexOutput71CDFSize) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput51CDF1},":","2")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex71Error3C")
if((splitStringByIndexReturn71C != 0 ))
{

        (Log(Unable to split 1st Datafile size))
        (Eject)

}




 @Rule (name = "ManageTablespace_splitStringByIndex513C")
if((splitStringByIndexReturn41C = 0 ) && (splitStringByIndexOutput41CDFstmt !:= ""))
{

  
  (splitStringByIndexReturn512C,splitStringByIndexOutput51CDF2) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput41CDFstmt},",","2")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex51Error3C")
if((splitStringByIndexReturn512C != 0 ))
{

        (Log(Unable to split 2nd Datafile ))
        (Eject)

}



 @Rule (name = "ManageTablespace_splitStringByIndex613C")
if((splitStringByIndexReturn512C = 0 ) && (splitStringByIndexOutput51CDF2 !:= ""))
{

  
  (splitStringByIndexReturn612C,splitStringByIndexOutput612CDFName) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput51CDF2},":","1")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex61Error3C")
if((splitStringByIndexReturn612C != 0 ))
{

        (Log(Unable to split 2nd Datafile name))
        (Eject)

}


 @Rule (name = "ManageTablespace_splitStringByIndex713C")
if((splitStringByIndexReturn612C = 0 ) && (splitStringByIndexOutput51CDF2 !:= ""))
{
  
  (splitStringByIndexReturn712C,splitStringByIndexOutput712CDFSize) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput51CDF2},":","2")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex71Error3C")
if((splitStringByIndexReturn712C != 0 ))
{

        (Log(Unable to split 2nd Datafile size))
        (Eject)

}







 @Rule (name = "ManageTablespace_splitStringByIndex513C")
if((splitStringByIndexReturn41C = 0 ) && (splitStringByIndexOutput41CDFstmt !:= ""))
{

  
  (splitStringByIndexReturn513C,splitStringByIndexOutput51CDF3) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput41CDFstmt},",","3")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex51Error3C")
if((splitStringByIndexReturn513C != 0 ))
{

        (Log(Unable to split 3rd Datafile ))
        (Eject)

}



 @Rule (name = "ManageTablespace_splitStringByIndex613C")
if((splitStringByIndexReturn513C = 0 ) && (splitStringByIndexOutput51CDF3 !:= ""))
{

  
  (splitStringByIndexReturn613C,splitStringByIndexOutput61CDFName3) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput51CDF3},":","1")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex61Error3C")
if((splitStringByIndexReturn613C != 0 ))
{

        (Log(Unable to split 3rd Datafile name))
        (Eject)

}


 @Rule (name = "ManageTablespace_splitStringByIndex713C")
if((splitStringByIndexReturn613C = 0 ) && (splitStringByIndexOutput51CDF3 !:= ""))
{
  
  (splitStringByIndexReturn713C,splitStringByIndexOutput71CDFSize3) = splitStringByIndex(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput51CDF3},":","2")
  
}

 @Rule (name = "ManageTablespace_splitStringByIndex71Error3C")
if((splitStringByIndexReturn713C != 0 ))
{

        (Log(Unable to split 3rd Datafile size))
        (Eject)

}






@Rule (name = "CompareValues_alterEmpty")
if ((splitStringByIndexOutput1Count := "0") && (splitStringByIndexOutput2Count := "0"))
{
        (sendEmailReturn4,sendEmailOutput4)=IgnioSystemLib:SendMail(${MailID},"","","","Ignio tried to resize the Tablespace, But there is no enough space in filesystem","Tablespace Utilization Status of ${TablespaceName} in database ${DBInstanceName} and Incident Number is : ${IncidentNumber}")
                
}
@Rule (name = "CompareValues_ErrorsendMail4")
if(( sendEmailReturn4 != 0 ))
{                
        (Log(Failed to send mail))
        (Eject)
}

@Rule (name = "CompareValues_ErrorAlterNull")
if(( sendEmailReturn4 = 0 ))
{
        
        (Log(Since there is no enough space in filesystem, Tablespace cannot be resized))
        (Eject)
}



@Rule (name = "CompareValues_add2nos1")
if ((splitStringByIndexOutput1Count !:= "") && (splitStringByIndexOutput2Count !:= ""))
{
	
(addTwoNumbersReturn1,addTwoNumbersOutput1)=addTwoNumbers(${DomainName},${HostLoginName},${HostName},${splitStringByIndexOutput1Count},${splitStringByIndexOutput2Count})
	
}

@Rule (name = "CompareValues_Erroradd2nos1")
if(( addTwoNumbersReturn1 != 0 ))
{		
	(Log(Failed to add nos))
	(Eject)
}

@Rule (name = "CompareValues_Maxadd2nos1")
if(( addTwoNumbersReturn1 = 0 ) && ( addTwoNumbersOutput1 > 3 ))
{
	
     (Log(Alter command reached max level))
     (Eject)
}

 @Rule (name = "ManageTablespace_addDatafile1")
if((splitStringByIndexOutput61DFName !:= "") && (splitStringByIndexOutput71DFSize !:= "") && (splitStringByIndexOutput2Count := "1") && ( splitStringByIndexOutput1Count <= 2 ))
{
         (addDataFileReturn1,addDataFileOutput1)=addDataFile(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}),${splitStringByIndexOutput61DFName},${splitStringByIndexOutput71DFSize})
}

 @Rule (name = "ManageTablespace_addDatafile2")
if((splitStringByIndexOutput91DFName2 !:= "") && (splitStringByIndexOutput101DFSize2 !:= "") && (splitStringByIndexOutput2Count := "2") && ( splitStringByIndexOutput1Count <= 1 ))
{
         (addDataFileReturn2,addDataFileOutput2)=addDataFile(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}),${splitStringByIndexOutput91DFName2},${splitStringByIndexOutput101DFSize2})
}

 @Rule (name = "ManageTablespace_addDatafile3")
if((splitStringByIndexOutput131DFName3 !:= "") && (splitStringByIndexOutput141DFSize3 !:= "") && (splitStringByIndexOutput2Count := "3") && ( splitStringByIndexOutput1Count = 0 ))
{
         (addDataFileReturn3,addDataFileOutput3)=addDataFile(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}),${splitStringByIndexOutput131DFName3},${splitStringByIndexOutput141DFSize3})
}


 @Rule (name = "ManageTablespace_addDatafileError1")
if((addDataFileReturn1 != 0 ) || (addDataFileReturn2 != 0 ) || (addDataFileReturn3 != 0 ))
{

        (Log(Failed to execute the block to add the Datafile ))
        (Eject)

}

 @Rule (name = "ManageTablespace_addDatafileError2")
if((addDataFileOutput1 := "NULL" ) || (addDataFileOutput2 := "NULL" ) || (addDataFileOutput3 := "NULL" ))
{

        (Log(Unable to add the Datafile ))
        (Eject)

}



 @Rule (name = "ManageTablespace_addDatafileSuccess")
if((addDataFileOutput1 := "Success" ) || (addDataFileOutput2 := "Success" ) || (addDataFileOutput3 := "Success" ))
{

(getTablespaceUsedPercentReturn31,getTablespaceUsedPercentOutput31) = getTablespaceUsedPercent(${HostName},${DBLoginName},${DBLoginPassword},${DBInstanceName},fn:toUpperCase(${getTablespaceOutput1}))

}

@Rule (name = "HandleTablespaceUtilization_Error1getTablespaceUsedPercent31")
if(( getTablespaceUsedPercentReturn31 != 0 ))
{
        (Log(Failed to execute the block to convert decimal to integer on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_Error2getTablespaceUsedPercent31")
if(( getTablespaceUsedPercentOutput31 := "NULL" ))
{
        (Log(Failed to convert decimal ${getTSUsedSizeUsingPercentageOutput31} to integer in database ${DBInstanceName} on the host ${HostName}))
        (Eject)
}

@Rule (name = "HandleTablespaceUtilization_compareValues31")
if(( getTablespaceUsedPercentOutput31 !:= "NULL" ))
{

        (CompareReturn31,CompareOutput31)=compareValues(${DomainName},${HostLoginName},${HostName},"84",${getTablespaceUsedPercentOutput31})

}

@Rule (name = "CompareValues_ErrorCompareValues31")
if(( CompareReturn31 != 0 ))
{
        (Log(Failed to compare Threshold values on host ${HostName}))
        (Eject)
}


@Rule (name = "CompareValues_ResolvedForValue2IsGreaterOrEqualToValue21")
if((( CompareReturn31 = 0 ) && ( CompareOutput31 = 0 )) && ((splitStringByIndexOutput2Count = 1) && (addDataFileOutput1 := "Success" ) && (splitStringByIndexOutput2addsize <= 0)))
{

        (sendEmailReturn2,sendEmailOutput2)=IgnioSystemLib:SendMail(${MailID},"","","","The New Datafile ${splitStringByIndexOutput61DFName} with ${splitStringByIndexOutput71DFSize} is added and the Current Tablespace threshold value is ${getTablespaceUsedPercentOutput31} ","Tablespace Utilization Status of ${TablespaceName} in database ${DBInstanceName} and Incident Number is : ${IncidentNumber}")
}


@Rule (name = "CompareValues_ResolvedForValue2IsGreaterOrEqualToValue22")
if ((( CompareReturn31 = 0 ) && ( CompareOutput31 = 0 )) && ((splitStringByIndexOutput2Count = 2) && (addDataFileOutput1 := "Success" ) && (addDataFileOutput2 := "Success" ) && (splitStringByIndexOutput2addsize <= 0)))
{
        (sendEmailReturn2,sendEmailOutput2)=IgnioSystemLib:SendMail(${MailID},"","","","The New Datafile ${splitStringByIndexOutput61DFName} with ${splitStringByIndexOutput71DFSize} is added, ${splitStringByIndexOutput91DFName2} with ${splitStringByIndexOutput101DFSize2} is added and the Current Tablespace threshold value is ${getTablespaceUsedPercentOutput31} ","Tablespace Utilization Status of ${TablespaceName} in database ${DBInstanceName} and Incident Number is : ${IncidentNumber}")
}


@Rule (name = "CompareValues_ResolvedForValue2IsGreaterOrEqualToValue23")
if ((( CompareReturn31 = 0 ) && ( CompareOutput31 = 0 )) && ((splitStringByIndexOutput2Count = 3) && (addDataFileOutput1 := "Success" ) && (addDataFileOutput2 := "Success" ) && (addDataFileOutput3 := "Success" ) && (splitStringByIndexOutput2addsize <= 0)))
{
        (sendEmailReturn2,sendEmailOutput2)=IgnioSystemLib:SendMail(${MailID},"","","","The New Datafile ${splitStringByIndexOutput61DFName} with ${splitStringByIndexOutput71DFSize} is added, ${splitStringByIndexOutput91DFName2} with ${splitStringByIndexOutput101DFSize2} is added, ${splitStringByIndexOutput131DFName3} with ${splitStringByIndexOutput141DFSize3} is added and the Current Tablespace threshold value is ${getTablespaceUsedPercentOutput31} ","Tablespace Utilization Status of ${TablespaceName} in database ${DBInstanceName} and Incident Number is : ${IncidentNumber}")
}


@Rule (name = "CompareValues_ResolvedForValue2sendMail2")
if(( sendEmailReturn2 = 0 ))
{
        
        
          (Log(New Datafile is added and Tablespace ${TablespaceName} resized successfully in the database ${DBInstanceName} on the host ${HostName} and mail has been sent to the mail id ${MailID}))
        (Resolved("Success"))

}

@Rule (name = "CompareValues_ErrorsendMail3")
if(( sendEmailReturn2 != 0 ))
{
        (Log(Failed to send mail))
        (Eject)
}

@Rule (name = "CompareValues_ResolvedForValue2IsLessThanValue1")
if(( CompareReturn31 = 0 ) && ( CompareOutput31 = 1 ))
{
        
        (sendEmailReturn3,sendEmailOutput3)=IgnioSystemLib:SendMail(${MailID},"","","","Ignio tried to resize the Tablespace, But there is no enough space in filesystem","Tablespace Utilization Status of ${TablespaceName} in database ${DBInstanceName} and Incident Number is : ${IncidentNumber}")
                
}
@Rule (name = "CompareValues_ErrorsendMail2")
if(( sendEmailReturn3 != 0 ))
{                
        (Log(Failed to send mail))
        (Eject)
}

@Rule (name = "CompareValues_ErrorThreshold")
if(( sendEmailReturn3 = 0 ))
{
        
        (Log(Since there is no enough space in filesystem, Tablespace cannot be resized))
        (Eject)
}

