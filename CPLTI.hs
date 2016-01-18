module CPLTI where

import CPLBasic
import IOExts
import GlaExts
import Array

infix  4  ==, <, <=, >=, >
infixr 3  &&
infixr 2  ||

-------------- Types ----------------------------------
-- TiPrim contains the open, high, low, close, and volume of a company
type Arr a = Array Int (Maybe a)
data TiPrim = TiPrim (Array Int [Maybe Price],
			Int, Arr Price, Arr Price, Arr Price, Arr Price, Arr Int)
data Ti = Ti (TiPrim)
data VMC c = VMC (Ti -> (c,Ti))
type Pattern = (Ind a c, Num a, Num c, Compare c b) => (Pat a b)
type TI = (Logic c, Compare b c, Ind a b) => a -> b
-------------- end of Types ---------------------------

---------------- memoization starts ------------------
mem i fname = \b -> 
		 do r <- fname b
		    writeM i r

-- update an array element at index i with v
update arr i v = let (a,b) = bounds arr
		 in array (a,b) (zip [a..b] (aux arr [a..b] i v))

aux arr [] i v = []
aux arr (j:x) i v = if (j Prelude.== i) then v:(map (arr !) x)
				      else (arr!j):(aux arr x i v) 

-- assign updates mem which is an array of list
assign mem n v = let (a,b) = bounds mem
		     ti = mem!n
		     ti' = v:ti		 
		 in update mem n ti'

writeM n v = VMC (\ t@(Ti (TiPrim(mem,range,t1,t2,t3,t4,t5))) -> 
			let mem' = assign mem n v
			in (v,(Ti (TiPrim(mem',range,t1,t2,t3,t4,t5)))))

readM n = VMC (\ t@(Ti (TiPrim(mem,range,_,_,_,_,_))) -> (mem ! n, t))

---------- some shorthand functions
--(.!) aList idx = do lst <- aList 
--		    return (lst !! (idx - 1)) 
(.!) aList idx = aList !! (idx -1)
-------------- memoization ends ----------------------

----------------------- Loading Technical indicator from File ---------
loadTi :: String -> IO (TiPrim,Int)
loadTi fname =
  do str <- readFile ("data/ascii/" ++ fname)
     let s = lines str
     let noOfbar = (length s) - 1
     let ts = tail s 
     let (os,hs,ls,cs,vs) = mkLst 1 ts ([],[],[],[],[])
     let oA = array (1,noOfbar) os 
     let hA = array (1,noOfbar) hs 
     let lA = array (1,noOfbar) ls
     let cA = array (1,noOfbar) cs 
     let vA = array (1,noOfbar) vs
     let lst = []
     let mem = array (1,5) [(1,lst),(2,lst), (3,lst),(4,lst),(5,lst)]
     return (TiPrim (mem,noOfbar, oA, hA, lA, cA, vA), noOfbar)

mkLst i [] rs = rs
mkLst i (t:ts) rs = 
	let (opens,highs,lows,closes,vols) = mkLst (i+1) ts rs 
	in  ((i,Just (read ((words t)!!1))):opens,
             (i,Just (read ((words t)!!2))):highs,
             (i,Just (read ((words t)!!3))):lows,
             (i,Just (read ((words t)!!4))):closes,
	     (i,Just (read ((words t)!!5))):vols)

----------------------- Virtual Machine --------------------------
instance Monad VMC where
 return c = VMC (\t -> (c,t))
 (>>=) (VMC ti) f = VMC ((\ (c,t) ->
			    let VMC ti' = f c in ti' t) . ti)

zeroProcess :: VMC (Maybe b)
zeroProcess = VMC (\t -> (Nothing,t))

instance Functor VMC where
 fmap f (VMC ti) = VMC (\t -> let (c,t') = ti t in (f c,t'))

runVMC (VMC ti) tiPrim bar = fst (ti (Ti tiPrim))

getState ti = VMC ((\ (c,state) -> (state,state)) . ti)

-- updBar (VMC f) updF bar = VMC (\ (Ti (state,a)) -> f (Ti (state,updF a bar)))
-- baredTi (VMC f) = \b -> VMC (\ (Ti (state,a)) -> f (Ti (state,b)))

runVMCs ti tiPrim bs = 
   let (VMC newf) = sequence (map ti bs)
   in  fst (newf (Ti tiPrim))

------------------------------- Primitive Technical Indicators ------------

close' b = VMC (\ t@(Ti (TiPrim(_,range,_,_,_,close,_))) -> 
		 (close ! b,t))
open' b = VMC (\ t@(Ti (TiPrim(_,range,open,_,_,_,_))) -> 
		 (open ! b, t))
high' b = VMC (\ t@(Ti (TiPrim(_,range,_,high,_,_,_))) -> 
		 (high ! b, t))
low' b = VMC (\ t@(Ti (TiPrim(_,range,_,_,low,_,_))) -> 
		 (low ! b, t))

------------------------- Classes for constraints ------------------------

class (Fractional b) => Ind a b | a -> b where
 close, open, low, high :: a -> b
 cast :: a -> b
 cond :: (a -> VMC (Maybe Bool))-> (a -> b)-> (a -> b) -> a -> b
 (.#) :: (a->b) -> a -> (a->b)

instance Ind Bar (VMC (Maybe Price)) where
 close  = close' 
 open  = open' 
 low  =  low' 
 high  =  high' 
 -- cast (I# i) = return (Just (F# (int2Float# i)))
 cast b =  return (Just (Prelude.fromInteger (toInteger (b::Bar))))
 cond a e1 e2 b =   do a'<-a b
		       case a' of
			 (Just a1) -> if a1 then (e1 b) else (e2 b)
			 Nothing -> return Nothing
 (.#) ind n = \b -> if (b Prelude.<= n) then return Nothing 
					else ind (b Prelude.- n)


instance Ind Lmk Lmk where
 close = id 
 open  = id 
 low  = id 
 high = id 
 cast = id
 cond a b c = id 
 (.#) ind n = ind

instance Eq (VMC (Maybe Price)) where
 (==) a b =  False

instance Show (VMC (Maybe Price)) where
 showsPrec _ _ = error "IO (Maybe Price)"

instance (Ind a b) => Show (a->b) where
 showsPrec _ _ = error "Cant"

instance (Ind a b) => Eq (a->b) where

instance Enum (VMC (Maybe Price)) where
 toEnum n = return (Just (toEnum n))
 fromEnum _ = (fromEnum 0)

instance Enum (Maybe Price) where
 toEnum n = (Just (toEnum n))
 fromEnum _ = (fromEnum 0)

instance Num (VMC (Maybe Price)) where
 (+) = liftIO2 (Prelude.+) 
 (-) = liftIO2 (Prelude.-) 
 (*) = liftIO2 (Prelude.*)
 abs = liftIO1 (Prelude.abs)
 signum = liftIO1 (Prelude.signum)
 fromInteger a = return (Just (Prelude.fromInteger a))

instance Num (Maybe Price) where
 (+) = liftMaybe (Prelude.+) 
 (-) = liftMaybe (Prelude.-) 
 (*) = liftMaybe (Prelude.*)
 abs = liftMaybe1 (Prelude.abs)
 signum = liftMaybe1 (Prelude.signum)
 fromInteger a = (Just (Prelude.fromInteger a))

instance Num Lmk where
 (+) = liftT
 (-) = liftT
 (*) = liftT
 abs (T a) = T (Prelude.abs a)
 signum (T a) = T (Prelude.signum a)
 fromInteger _ = T 0

instance (Ind a b) => Num (a -> b) where
 (+) x y = \b -> (x b) + (y b)
 (-) x y = \b -> (x b) - (y b)
 (*) x y = \b -> (x b) * (y b)
 abs x = \b -> abs (x b)
 signum x = \b -> signum (x b)
 fromInteger x = \b -> fromInteger x

instance (Ind a b) => Fractional (a -> b) where
 (/) x y = \b -> (x b) / (y b)
 fromRational x = \b -> fromRational x

instance Fractional (VMC (Maybe Price)) where
 a / b = do r1 <- a
	    r2 <- b
	    case (r1,r2) of
		(Just a1,Just b1) -> return (Just (a1 Prelude./ b1))
		(_,_) -> return Nothing
 fromRational n = return (Just (Prelude.fromRational n))

instance Fractional (Maybe Price) where
 a / b = let r1 = a
	     r2 = b
	 in  case (r1,r2) of
		(Just a1,Just b1) -> (Just (a1 Prelude./ b1))
		(_,_) ->  Nothing
 fromRational n = (Just (Prelude.fromRational n))

instance Fractional Lmk where
 (/) = liftT
 fromRational _ = T 0

liftIO1 op a = do r <- a
	     	  case r of
			Nothing -> return Nothing
			(Just b) -> return (Just (op b))

liftIO2 op a b = do r1<-a
		    r2<-b
		    return (liftMaybe op r1 r2)

liftMaybe1 op a = case a of
		  Nothing -> Nothing		  
		  Just b -> Just (op b)

liftMaybe op a b = case (a,b) of
		  (Nothing, _) -> Nothing
		  (_,Nothing) -> Nothing
		  (Just c, Just d) -> Just (op c d)

class (Logic b, Num a) => Compare a b | a -> b where
 (>),(<),(==),(<=),(>=) :: a -> a -> b

instance Compare Int Bool where
 (>) = (Prelude.>)
 (<) = (Prelude.<)
 (==) = (Prelude.==)
 (>=) = (Prelude.>=)
 (<=) = (Prelude.<=)

instance Compare Lmk Lmk where
 (>) = liftT
 (<) = liftT
 (==) = liftT
 (>=) = liftT
 (<=) = liftT

liftT (T a) (T b) = T (a `max` b)

instance Compare (VMC (Maybe Price)) (VMC (Maybe Bool)) where
 (>) = cmp (Prelude.>)
 (<) = cmp (Prelude.<)
 (==)= cmp (Prelude.==)
 (<=)= cmp (Prelude.<=)
 (>=)= cmp (Prelude.>=)

cmp op = \a b -> do r1<-a
		    r2<-b
		    case (r1,r2) of
			(Just a1, Just b1) -> return (Just (op a1 b1))
			(_,_) -> return Nothing


instance (Ind a b, Logic (a->c), Compare b c) => Compare (a -> b) (a -> c) where
 (>) x y = \b -> (x b) CPLTI.> (y b)
 (<) x y = \b ->  (x b) CPLTI.< (y b)
 (==) x y = \b ->  (x b) CPLTI.== (y b)
 (<=) x y = \b ->   (x b) CPLTI.<= (y b)
 (>=) x y = \b ->   (x b) CPLTI.>= (y b)


class Logic a where
 (&&), (||) :: a -> a -> a


instance Logic Bool where
 (&&) True True = True
 (&&) _ _ = False
 (||) False False = False
 (||) _ _ = True


instance Logic (VMC (Maybe Bool)) where
 (&&) a b = do r1 <- a
	       r2 <- b
	       case (r1,r2) of
		  (Just a1,Just b1) -> return (Just (a1 Prelude.&& b1))
		  (_, _) -> return Nothing
 (||) a b = do r1 <- a
	       r2 <- b
	       case (r1,r2) of
		  (Just a1,Just b1) -> return (Just (a1 Prelude.|| b1))
		  (_, _) -> return Nothing

instance Logic Lmk where
 (&&) = liftT
 (||) = liftT

