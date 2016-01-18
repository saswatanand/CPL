module CPL where

import System
import CPUTime
import IOExts
import CPLBasic
import CPLTI
import CPLPattern
import BuiltinPatterns
import Array
import Numeric

--system--
import MovingAvg

--user created start--
import C1
import ExpMovingAvg
import Hill3
import Indt1
import Indt2
import Hill2
--user created end--


--------- for timing without interface -------------------------
doComp :: Pattern -> String -> IO ()
doComp pat cname
 = do (tiPrim,b) <- loadTi cname
      t1 <- getCPUTime             
      let insts = runVMC (solve (1,b) pat tiPrim) tiPrim 0
      print insts      
      t2 <- getCPUTime     
      putStrLn ("CPU Time : " ++ showFFloat Nothing ((fromInteger (t2 Prelude.-t1))/1000000000000) " seconds.")

allComps :: Pattern -> String -> IO ()
allComps pat arg
 =  do	  
	  let filename = arg
	  content <- readFile filename
	  let companies = lines content
	  mapM_ (doComp pat) companies
	  print companies
	  print "CPL ... done!"

--------- end of timing ----------------------------------------

--------- for viewing with Java interface ---------------------
--------- for displaying pattern instances 
browse :: String -> Pattern -> IO ()
browse cname pat
 = do (tiPrim,b) <- loadTi cname
      let insts = runVMC (solve (1,b) pat tiPrim) tiPrim 0
      putStrLn ("_DIRECTIVE_BROWSE " ++ fun insts)
         where fun lmss = let tmp = foldr (\l1 l2 -> (init.tail.show) l1 ++ ('%':l2)) "" lmss
			  in if null tmp then [] else cname ++ ('@':init tmp)

--------- for displaying indicators
evalDisplay func cname = do (tiPrim,b) <- loadTi cname
			    putStrLn ("_DIRECTIVE_TI " ++
					 show(cname) ++ "@" ++ show(b) ++ "@" ++ 
					 showTI(runVMCs func tiPrim [1..b]))
					

showTI xs = case xs of 
		 [] -> ""
		 (a:x) -> case a of 
			       Nothing -> "-1," ++ showTI(x)
			       Just b -> (show ((fromIntegral (round(b*100)))/100)) ++ ","++showTI(x)
------------------------------- end of interface ---------------------------