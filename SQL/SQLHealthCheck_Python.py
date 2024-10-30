import pyodbc
import psycopg2
import sys
from datetime import datetime


def sqlhealth(servername, instancename, username, pwd, runnser, stopser):
    print(servername)
    print(instancename)
    if instancename != "MSSQLSERVER" :
        servername = servername+"\\"+instancename
    instance = instancename
    print(servername)
    print(username)
    print(pwd)

    print("=====================================SERVICE STATUS==========================================")

    print("Running Services : ",runnser)

    print("Stopped Services : ",stopser)

    print("=====================================CONNECTING TO SQL SERVER==========================================")

    conn = pyodbc.connect(Driver='{SQL Server}', Server=servername, Database='master',port=1433, UID=username, PWD=pwd)
    print("SQL CONNECTED")

    print("=====================================GET ONLINE DBS==========================================")

    cursor1 = conn.cursor()
    cursor1.execute(
        """SELECT name FROM sys.databases where state_desc = 'ONLINE' """)
    ond = []
    records1 = cursor1.fetchall()
    for ondbs in range(len(records1)):
        ond.append(records1[ondbs][0])
    print("ONLINE : ", ond)

    # postgress query insert

    print("=====================================GET OFFLINE DBS==========================================")

    cursor11 = conn.cursor()
    cursor11.execute(
        """SELECT name FROM sys.databases where state_desc = 'OFFLINE'  """)
    ofd = []
    records11 = cursor11.fetchall()
    for ofdbs in range(len(records11)):
        print(records11[ofdbs][0])
        ofd.append(records11[ofdbs][0])
    print("OFFLINE : ", ofd)
    # postgress query insert

    print("=====================================GET RESTORING DBS==========================================")

    cursor12 = conn.cursor()
    cursor12.execute(
        """SELECT name FROM sys.databases where state_desc = 'RESTORING' """)
    restd = []
    records12 = cursor12.fetchall()
    for resdbs in range(len(records12)):
        print(records12[resdbs][0])
        restd.append(records12[resdbs][0])
    print("RESTORING : ", restd)
    # postgress query insert

    print("=====================================GET RECOVERING DBS==========================================")
    cursor13 = conn.cursor()
    cursor13.execute(
        """SELECT name FROM sys.databases where state_desc = 'RECOVERING' """)
    recd = []
    records13 = cursor13.fetchall()
    for recdbs in range(len(records13)):
        print(records13[recdbs][0])
        recd.append(records13[recdbs][0])
    print("RECOVERING : ", recd)
    # postgress query insert

    print("=====================================GET BLOCKING SESSIONS==========================================")

    cursor2 = conn.cursor()
    getBlock = """SELECT r.session_id,r.blocking_session_id,DB_NAME(r.database_id) AS Database_Name, s.login_name,t.text as Query_Text FROM sys.dm_exec_requests r CROSS APPLY sys.dm_exec_sql_text(sql_handle) t INNER JOIN sys.dm_exec_sessions s ON r.session_id = s.session_id WHERE r.blocking_session_id <> 0 """
    cursor2.execute(getBlock)
    records2 = cursor2.fetchall()
    blockcount = len(records2)
    print("BLOCKING SESSIONS : ", blockcount)
    for row2 in records2:
        blockdetails = row2
        print("BLOCKING SESSIONS : ", row2)
        # postgress query insert

    print("=====================================GET DATA/LOG FILE SIZE=================================================")

    cursor3 = conn.cursor()
    getLogFile = """DECLARE @v_getLogReviews Nvarchar(1000);
        BEGIN
        WITH fs
        AS
        (
            SELECT database_id, TYPE, SIZE * 8.0 / 1024 SIZE
            FROM sys.master_files
        )


            SELECT STUFF(
          COALESCE(' , ' + RTRIM(name),     '') 
        + COALESCE(' = ' + RTRIM((SELECT SUM(SIZE) FROM fs WHERE TYPE = 0 AND fs.database_id = db.database_id) ), '') 
        + COALESCE(' = ' + RTRIM((SELECT SUM(SIZE) FROM fs WHERE TYPE = 1 AND fs.database_id = db.database_id) ),  '')
        , 1, 2, '') as sys
        FROM sys.databases db

        END;  """

    cursor3.execute(getLogFile)
    records3 = cursor3.fetchall()
    logf = []
    logf1 = []
    logf2 = []
    logcount = len(records3)
    print(logcount)
    for row3 in records3:
        logf.append(row3[0])
    logfs = [str(i) for i in logf]
    print(logfs)
    cursor3.close()

    print("=====================================GET ERROR LOGS===============================================")

    cursor4 = conn.cursor()
    getErr = """ CREATE TABLE #TempTable (Col1 varchar(1000), Col2 varchar(1000), Col3 varchar(1000))
        SET NOCOUNT ON
        INSERT INTO #TempTable
        EXEC xp_readerrorlog 0,1
        SELECT STUFF((SELECT ',,,' + CAST(Col1 AS varchar(1000)),':' + CAST(Col3 AS varchar(1000)) FROM #TempTable FOR XML PATH('')), 1 ,1, '') 
        DROP TABLE #TempTable """

    cursor4.execute(getErr)
    records4 = cursor4.fetchall()
    errlog=""
    line=0
    for e in records4[0][0].split(",,,"):
        if line ==10:
            break
        errlog += e + '\n'
        errlog = errlog.replace("'", '"')
        line +=1
    print("Error Log : ", errlog)
    cursor4.close()

    print("=====================================GET TOTAL DB SIZE===========================================")

    cursor5 = conn.cursor()
    getTotDBSize = """SELECT CONVERT(DECIMAL(10,2),(SUM(size * 8.00) / 1024.00 / 1024.00)) FROM master.sys.master_files """

    cursor5.execute(getTotDBSize)
    records5 = cursor5.fetchall()
    tot = records5[0][0]
    print("TotDBSize : ", tot)

    print("=====================================GET TOTAL LOG SIZE===========================================")
    totlog="0"
    try:
        cursor6 = conn.cursor()
        getTotLogSize = """ 
            SELECT STUFF(
              COALESCE(', ' + RTRIM(total_log_size_in_bytes/1024/1024),     '') 
            + COALESCE(', ' + RTRIM(used_log_space_in_bytes/1024/1024), '') 
            + COALESCE(', ' + RTRIM(used_log_space_in_percent/1024/1024),  '')
            , 1, 2, '')
          FROM sys.dm_db_log_space_usage; """

        cursor6.execute(getTotLogSize)
        records6 = cursor6.fetchall()
        totlog = records6[0][0]
    except:
        print("Logsize : ", totlog)

    print("=====================================GET DRIVE DETAILS=============================================")

    cursor7 = conn.cursor()
    getDiskSp = """ SELECT DISTINCT STUFF(
          COALESCE(', ' + RTRIM(volume_mount_point),     '') 
        + COALESCE(', ' + RTRIM(file_system_type), '') 
        + COALESCE(', ' + RTRIM(CONVERT(DECIMAL(18,2),total_bytes/1073741824.0)),  '')
        + COALESCE(', ' + RTRIM(CONVERT(DECIMAL(18,2),available_bytes/1073741824.0)),  '')
        + COALESCE(', ' + RTRIM(100 - (CAST(CAST(available_bytes AS FLOAT)/ CAST(total_bytes AS FLOAT) AS DECIMAL(18,2)) * 100 ) ) ,  '')
        , 1, 2, '')
      FROM sys.master_files 
    CROSS APPLY sys.dm_os_volume_stats(database_id, file_id) """

    cursor7.execute(getDiskSp)
    records7 = cursor7.fetchall()
    getDiskSp = records7[0][0]
    print("DiskDet : ", getDiskSp)

    print("=====================================GET CPU USAGE=============================================")

    # cpu idle,cpu usage sql
    cursor8 = conn.cursor()
    getCpu = """ SELECT STUFF(
          COALESCE(', ' + RTRIM(record.value('(./Record/SchedulerMonitorEvent/SystemHealth/SystemIdle)[1]', 'int')),     '') 
        + COALESCE(', ' + RTRIM(record.value('(./Record/SchedulerMonitorEvent/SystemHealth/ProcessUtilization)[1]', 'int')), '') 
        , 1, 2, '')
      FROM (
             SELECT TOP 1 CONVERT(XML, record) AS record
             FROM sys.dm_os_ring_buffers
             WHERE ring_buffer_type = N'RING_BUFFER_SCHEDULER_MONITOR'
             AND record LIKE '% %'
             ORDER BY TIMESTAMP DESC
    ) as cpu_usage """

    cursor8.execute(getCpu)
    records8 = cursor8.fetchall()
    getCpu = records8[0][0]
    print("CPUDet : ", getCpu)

    print("=====================================GET CURRENT DATE/TIME===================================")

    currtime = datetime.now()
    print("REPORT TIME : ", currtime)
    stats = "succ"

    print("====================================INSERT INTO POSTGRES DB===================================")

    '''pc = psycopg2.connect(user="ignio", password="TCSIgnio@12345", host="10.201.128.154", port="5432",
                          database="postgres")'''
    pc = psycopg2.connect(user="ignio",
                                  password="TCSIgnio@12345",
                                  host="10.201.128.154",
                                  port="5432",
                                  database="postgres")

    print("Postgres opened successfully")
    pcur = pc.cursor()

    pcur.execute("""insert into ddata.ignio_001_dbpostbatchhealthcheck (servername, instancename, runningservices, stoppedservices, drivedetails, totaldbsize, onlinedbs, offlinedbs, blockingsessions, errorlog, logfilesize, reporttime, status, restdb, recdb, cpuusage, dblogs) values ( %s,%s,%s ,%s ,%s ,%s ,%s ,%s ,%s ,%s ,%s ,%s, %s, %s, %s, %s, %s )""", (servername, instance, runnser, stopser, getDiskSp, tot, ond, ofd, blockcount, errlog, totlog, currtime, stats, restd, recd, getCpu, logfs))
    pc.commit()

    """outp = servername---instance---runnser---stopser---getDiskSp---tot---ond---ofd---blockcount---errlog---totlog---currtime---stats---restd---recd---getCpu---logfs
    print(outp)"""
    print("====================================POSTGRES INSERTION SUCCESSFULLY DONE===================================")

    default = ""



print("=========================================STARTING SQL HEALTHCHECK============================================")

# if __name__ == "__main__":
#    main(sys.argv[1])


servername = sys.argv[1]
instancenames = sys.argv[2]
runnser = sys.argv[3]
stopser = sys.argv[4]
user = sys.argv[5]
pwd = sys.argv[6]
user = "sepmdbc"
pwd = "SymantecDB12345"

ins = instancenames.split(",")
for instancename in ins:
    sqlhealth(servername, instancename, user, pwd, runnser, stopser)

#SGBCCSDB01 MSSQLSERVER SQLSERVER SQLAGENT
"""servername = "10.201.128.184"
instancename = "SEM5"
runnser = "SQLSERVER"
stopser = "AGENT"
user = "sepmdbc"
pwd = "SymantecDB12345"
sqlhealth(servername, instancename, user, pwd, runnser, stopser)"""