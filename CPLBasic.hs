module CPLBasic where

import Data.Generics

type Bar = Int
type Price = Float

-----------------------CPL datatypes----------------------------

------- Data types needed at PatState ------
{-data TI = Close | Open | Low | High 
	deriving (Show, Eq, Typeable, Data) 
-}
data PatD = Up | Down | Fby PatD PatD | Nil 
	deriving (Show, Typeable, Data) 
data Lmk = T Int deriving (Show, Eq, Typeable, Data) 
type Pat a b = (PatD, [a] -> [b])

------- Data types needed at InstState ------
type CompanyName = String
newtype Tree a = Tree (a,[Tree a]) deriving (Eq,Show)
type Inst = [Tree Bar]