module CPLPattern where

import CPLBasic
import CPLTI
import Lib


----------------------- Auxilary Functions -----------------------
lms :: PatD -> [Lmk]
lms Up = [T 1, T 2]
lms Down = [T 1, T 2]
lms (Fby p1 p2) = let n1 = lms p1
	 	      n2 = lms p2
		      l1=length n1
		      l2=length n2
		      n2'=genLandmarks l1 l2
		  in (n1 ++ n2')
 where genLandmarks n1 1 = []
       genLandmarks n1 n2 = (T (n1 Prelude.+ 1)):
				genLandmarks (n1 Prelude.+ 1) (n2 Prelude.- 1)

lowerT (T n) = n

ordApply f xs olist = f xs


------------------------- patterns ---------------------

(>.>) ::  (Pat a b) -> (Pat a b) -> (Pat a b)
(>.>) p1 p2 =let (a1,f1) = p1
		 (a2,f2) = p2
		 l1 = length (lms a1)
		 l2 = length (lms a2)
		 f = \xs -> f1 (take l1 xs) ++ 
					f2 (take l2 (drop (l1 Prelude.-1) xs))
	     in (Fby a1 a2, f)

(??) :: (Pat a b) -> ([a]->[b]) -> (Pat a b)
(??) p f = let (p1,f1) = p
	   in (p1, \xs -> f1 xs ++ f xs)

map' f [] = return []
map' f (l:ls) = do a <- f l
                   if (null a) then map' f ls
		 	       else do rest <- map' f ls
				       return ((Tree (l,[Tree (t,[])|t<-a])):rest)

extreme :: PatD -> PatD -> Bool
extreme Up Up = True
extreme Down Down = True
extreme Up Down = False
extreme Down Up = False
extreme (Fby p1 p2) p3 = extreme p2 p3
extreme p1 (Fby p2 p3) = extreme p1 p2

getStartPt :: Tree Bar -> Bar
getStartPt (Tree (a,t)) =  a

getEndPts :: Tree Bar -> [Bar]
getEndPts (Tree (a,t)) = case t of
		 	  [] -> [a]
			  (y:ys) -> concat (map getEndPts t)

join :: Inst -> Inst -> Inst
join [] ys = []
join xs [] = []
join (a:x) (b:y) = let a1 = getEndPts a
		       b1 = getStartPt b
		   in if (b1 `elem` a1) then (join2 a b):join x (b:y)
 				        else join (a:x) y

join2 :: Tree Bar -> Tree Bar -> Tree Bar
join2 a1@(Tree (a,[])) (Tree (b,t2)) = if (b Prelude.== a) 
						then Tree (a,t2)
						else a1
join2 (Tree (a,t1)) b1@(Tree (b,t2)) = Tree (a,map (\q->join2 q b1) t1)
		
filterM :: (a-> VMC Bool) -> [a] -> VMC [a]
filterM f [] = return []
filterM f (x:xs) = do b <- (f x)
		      if b then do rest <- filterM f xs
				   return (x:rest)			
			   else filterM f xs

treeToList :: Tree Bar -> [[Bar]]
treeToList (Tree (a,[])) = [[a]]
treeToList (Tree (a,t)) = map ([a]++) (concat (map treeToList t))

andM :: [VMC (Maybe Bool)] -> VMC Bool
andM [] = return True
andM (a:x) = do a'<-a 
		case a' of
		  Nothing -> return False
		  (Just b) -> do rest<-(andM x)
				 return (b Prelude.&& rest)

--------------------------- solvers -----------------------------
solve :: (Bar,Bar) -> Pattern -> TiPrim -> VMC ([[Bar]])
solve (b1,b2) pat tiPrim 
		  = do let (pA,fA::[Lmk]->[Lmk]) = pat
		       let ordlist = sort (map lowerT (fA (lms pA)))
		       let (pC,fC::[Bar]->[VMC (Maybe Bool)]) = pat
		       evalc_fast fC (evalp (b1,b2) pC Nothing) ordlist	       
		      
evalc :: ([Bar]->[VMC (Maybe Bool)]) -> VMC Inst -> [Bar] -> VMC ([[Bar]])
evalc f ins olist 
 = do insts <- ins      		 
      f' <- filterM (\xs -> andM (f xs)) (concat (map treeToList insts))
      return f'

evalc_fast :: ([Bar]->[VMC (Maybe Bool)]) -> VMC Inst -> [Bar] -> VMC ([[Bar]])
evalc_fast f ins olist 
 = do insts <- ins      		 
      f' <- filterM (\xs -> andM (ordlss olist (f xs))) (concat (map treeToList insts))
      return f'
  		 		     
evalp :: (Bar,Bar) -> PatD -> Maybe [Bar] -> VMC Inst
evalp (b1,b2) Up endPoints = do insts <- map' forEachStartPoint [b1..b2 Prelude.-1]
	 		        return insts
   where forEachStartPoint :: Bar -> VMC [Bar]	
   	 forEachStartPoint b = do (Just t) <- (close (b Prelude.+1) CPLTI.== close b)
				  if t then return [] else f b (b Prelude.+1)			  
	       where f maxi current | current Prelude.<= b2 =  
			do (Just t1) <- (close current CPLTI.< close maxi) 
			   (Just t2) <- (close current CPLTI.== close maxi) 
			   if t1 then return [] 
				 else if t2
				        then f maxi (current Prelude.+1)
					else case endPoints of
						   Nothing -> do rest <- f current (current Prelude.+1)
								 return (current:rest)
						   Just xs -> if (current `elem` xs)
							        then  do rest <- f current (current Prelude.+1)
									 return (current:rest)
								else f current (current Prelude.+1)
			            | otherwise = return []	
evalp (b1,b2) Down endPoints = do insts <- map' forEachStartPoint [b1..b2 Prelude.- 1]
		        	  return insts
   where forEachStartPoint :: Bar -> VMC [Bar]	
   	 forEachStartPoint b = do (Just t) <- (close (b Prelude.+1) CPLTI.== close b)
				  if t then return [] else f b (b Prelude.+1)		  
	       where f maxi current | current Prelude.<= b2 =  
			do (Just t1) <- (close current CPLTI.> close maxi) 
			   (Just t2) <- (close current CPLTI.== close maxi) 
			   if t1 then return [] 
				 else if t2
				        then f maxi (current Prelude.+1)
					else case endPoints of
						   Nothing -> do rest <- f current (current Prelude.+1)
								 return (current:rest)
						   Just xs -> if (current `elem` xs)
							        then  do rest <- f current (current Prelude.+1)
									 return (current:rest)
								else f current (current Prelude.+1)
			            | otherwise = return []
evalp (b1,b2) (Fby p1 p2) Nothing   = do insts2 <- evalp (b1,b2) p2 Nothing
				       	 let startPts = map getStartPt insts2
	   			         insts1 <- evalp (b1,b2) p1 (Just startPts)
				         return (join insts1 insts2)
evalp (b1,b2) (Fby p1 p2) endPoints = do insts2 <- evalp (b1,b2) p2 endPoints
				       	 let startPts = map getStartPt insts2
	   			         insts1 <- evalp (b1,b2) p1 (Just startPts)
				         return (join insts1 insts2)	



